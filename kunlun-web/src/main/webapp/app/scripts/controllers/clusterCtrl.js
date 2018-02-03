define(['app','d3','../charts/nodeCharts'],function(app,d3,nodeCharts){
	app.controller("clusterCtrl",["$scope", "$http", 'hs_es','hs_auto_refresh', '$timeout','$filter', '$rootScope', 
		function($scope, $http, hs_es,hs_auto_refresh, $timeout,$filter, $rootScope){
		$rootScope.app_nav = "首页 / 集群监控 / ES集群监控";
		 $(document).click(function(e) {
             $(".dropdown-menu").hide();
         });
		$scope.cluster = {
			"name":"",
			"status":"",
			"nodes":0,
			"shards":0,
			"store":0,
			"version":0,
			"node_pagination":{
				"currentPage":1,
				"pageSize":5,
				"totalNum":0,
				"pages":[]
			},
			"indices_pagination":{
				"currentPage":1,
				"pageSize":10,
				"totalNum":0,
				"pages":[]
			},
			"nodesList":[],
			"indicesList":[]
		};
		var nodeList = {};
		var cluster = {};
		function showNodes(){
			$http.get(hs_es.host_name + "/_nodes/stats").success(function(data){
				var reg = /[\w\W]*\[[\w\W]*\/([0-9\.]*):.*/;
				var n = 0;
				for(var node in data.nodes){
					nodeList[node] = data.nodes[node];
					var tmp = nodeList[node].ip[0].match(reg);
					if (tmp) nodeList[node].ip[0] = tmp[1];
					n ++;
				}
				var num = (n/$scope.cluster.node_pagination.pageSize).toString();
				if(num.indexOf(".") == -1){
					$scope.cluster.node_pagination.totalNum = parseInt(num);
				}else{
					$scope.cluster.node_pagination.totalNum = parseInt(num.substring(0,num.indexOf(".")))+1;
				}
				$scope.cluster.node_pagination.pages = [];
				for(var i=1;i<=$scope.cluster.node_pagination.totalNum;i++){
					$scope.cluster.node_pagination.pages.push(i);
				}
				$scope.showNodeList();
				waitter.hidden();
				$("#content").show();
			});
		}
		/*var records = [];*/
		$scope.showNodeList = function(currentPage){
			var records = [];
			$http.get(hs_es.host_name+"/_cat/shards?h=shard,node,index,prirep&v").success(function(data){
				/*var eles = data.split(/\s+/);*/
				var ss = data.split("\n");
				var idx1 = ss[0].indexOf("node");
				var idx2 = ss[0].indexOf("index");
				var idx3 = ss[0].indexOf("prirep");
				
				var col1,col2,col3,col4;
				//var records = [];
				for(var i=1;i<ss.length;i++){
					if (ss[i]) {
						col1 = ss[i].substring(0, idx1-1).trim();
						col2 = ss[i].substring(idx1, idx2-1).replace(/\s+/g,"");
						col3 = ss[i].substring(idx2, idx3-1).replace(/\s+/g,"");
						col4 = ss[i].substring(idx3, ss[i].length-1).trim();
						if(col1.indexOf(" ") != -1){
							col1 = col1.split(" ")[0];
						}
						records.push({
							"shard":col1,
							"node":col2,
							"index":col3,
							"prirep":col4
						})
					}
				}
				$scope.cluster.nodesList = {};
				if(currentPage){
					$scope.cluster.node_pagination.currentPage = currentPage;
				}
				var from = ($scope.cluster.node_pagination.currentPage-1)*$scope.cluster.node_pagination.pageSize;
				var to = $scope.cluster.node_pagination.currentPage*$scope.cluster.node_pagination.pageSize;
				var n = 0;
				$scope.sysarr = [];
				var m = 0;
				for(var node in nodeList){
					var count = 0;
					var countP = 0;
					var countR = 0;
					if(n >= from && n < to){
						$scope.cluster.nodesList[node]=nodeList[node];
						var name = nodeList[node].name;
								for(var j=0;j<records.length;j++){
									if(records[j].node == name){//如果属于该节，则判断该节点下是否已存在该shard
										var b = false;
										if(records[j].prirep=="r"){
											countR++;
										}
	                                    if(records[j].prirep=="p"){
											countP++;
										}
										count++;
									}
								};
						$scope.sysarr.push({
							"per_load":$filter("number")(nodeList[node].os.load_average[0]*100,0),
							"per_disk":$filter("number")((nodeList[node].fs.total.total_in_bytes-nodeList[node].fs.total.available_in_bytes)/nodeList[node].fs.total.total_in_bytes*100,0),
							"per_cpu":$filter("number")(100 - nodeList[node].os.cpu.idle,0),
							"per_mem":$filter("number")(nodeList[node].os.mem.used_percent,0),
							"per_count":count,
							"per_countR":countR,
							"per_countP":countP
						});
					}
					n ++;
				}
				records = null;
				
			});
			var a = false;
			$http.get(hs_es.host_name+"/_cluster/stats").success(function(data){
				$scope.cluster.name = data.cluster_name;
				$scope.cluster.status = data.status;
				$scope.cluster.nodes = data.nodes.count.total;
				$scope.cluster.shards = data.indices.shards.total;
				$scope.cluster.store = convertData(data.nodes.fs.total_in_bytes);
				$scope.cluster.usedstore = convertData(Number(data.nodes.fs.total_in_bytes)-Number(data.nodes.fs.available_in_bytes));
				$scope.cluster.version = data.nodes.versions;
				setHealth();
				/***集群名称****/
				cluster["name"] = data.cluster_name;
				cluster["children"] = [];
				$http.get(hs_es.host_name+"/_cat/nodes?h=name").success(function(data){
					/***封装集群中节点***/
					var eles = data.split(/\s+/);
					for(var i=0;i<eles.length;i++){
						if(eles[i]){
							var node = {};
							node["name"] = eles[i];
							node["children"] = [];
							cluster.children.push(node);
						}
					}
				});
				a = true;
			});
			/*$scope.cluster.nodesList = {};
			if(currentPage){
				$scope.cluster.node_pagination.currentPage = currentPage;
			}
			var from = ($scope.cluster.node_pagination.currentPage-1)*$scope.cluster.node_pagination.pageSize;
			var to = $scope.cluster.node_pagination.currentPage*$scope.cluster.node_pagination.pageSize;
			var n = 0;
			$scope.sysarr = [];
			var m = 0;
			for(var node in nodeList){
				var count = 0;
				var countP = 0;
				var countR = 0;
				if(n >= from && n < to){
					$scope.cluster.nodesList[node]=nodeList[node];
					var name = nodeList[node].name;
							for(var j=0;j<records.length;j++){
								if(records[j].node == name){//如果属于该节，则判断该节点下是否已存在该shard
									var b = false;
									if(records[j].prirep=="r"){
										countR++;
									}
                                    if(records[j].prirep=="p"){
										countP++;
									}
									count++;
								}
							};
					if(count == 0){
						count = "<center style='margin-top:150px;'><i class=\"fa fa-spinner fa-spin fa-3x\"></i></center>";
					}		
					$scope.sysarr.push({
						"per_load":$filter("number")(nodeList[node].os.load_average[0]*100,0),
						"per_disk":$filter("number")((nodeList[node].fs.total.total_in_bytes-nodeList[node].fs.total.available_in_bytes)/nodeList[node].fs.total.total_in_bytes*100,0),
						"per_cpu":$filter("number")(100 - nodeList[node].os.cpu.idle,0),
						"per_mem":$filter("number")(nodeList[node].os.mem.used_percent,0),
						"per_count":count,
						"per_countR":countR,
						"per_countP":countP
					});
				}
				n ++;
			}*/
		};
		
		$scope.per_load_opt = {animate:false,barColor:'#c77f54',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_disk_opt = {animate:false,barColor:'#f18d2c',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_cpu_opt = {animate:false,barColor:'#ca607e',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		$scope.per_mem_opt = {animate:false,barColor:'#d8ad45',trackColor:'#EEEEEE',scaleColor:false,lineWidth:6,lineCap:'circle',size:50,rotate : -90};
		
		/**健康度**/
		function setHealth(){
			var c=document.getElementById("dashboard_health");
			var cxt=c.getContext("2d");
			cxt.fillStyle = $scope.cluster.status;
			cxt.beginPath();
			cxt.arc(150,70,70,0,Math.PI*2,true);
			cxt.closePath();
			cxt.fill();
		}
		
		function convertData(dataBytes){
			var data_level = "B";
			var data_level_num = 0;
			while(dataBytes > 1024){
				dataBytes /= 1024;
				data_level_num ++;
			}
			switch(data_level_num){
				case 1:
					data_level = "KB";
					break;
				case 2:
					data_level = "MB";
					break;
				case 3:
					data_level = "GB";
					break;
				case 4:
					data_level = "TB";
					break;
				default: 
					data_level = "B";
					break;
			}
			return $filter("number")(dataBytes,2)+data_level;
		}
		function dataformart(input){
			if(input < 1024){
				return input.toFixed(2)+"b";
			}else if(input/1024 >=1 && input/1024 <1024){
				return (input/1024).toFixed(2)+"KB";
			}else if (input/1024/1024 >=1 && input/1024/1024< 1024) {
				return (input/1024/1024).toFixed(2)+"MB";
			}else if (input/1024/1024/1024 >= 1 && input/1024/1024/1024 < 1024) {
				return (input/1024/1024/1024).toFixed(2)+"GB";
			}else if (input/1024/1024/1024/1024 >=1 && input/1024/1024/1024/1024 < 1024) {
				return (input/1024/1024/1024/1024).toFixed(2) +"TB";
			}
		}
		var timerId;
		$scope.timeout = function(){
			timerId = $timeout(function(){
				waitter.show();
				//$scope.timeout();
			},0);
		};
		/*$scope.$on('$destroy', function() {
			$timeout.cancel(timerId);
		});*/
		var rr =[];
		/**获取文档及索引**/
		//var indicesList = {};
		function showIndeces(){
			$http.get(hs_es.host_name + "/_cat/indices?h=health,index,pri,docs.count,docs.deleted,store.size,pri.store.size&v").success(function(data){
				var rr1 = [];
				var split=data.split("\n"); 
				var index1 = split[0].indexOf("index");
				var index2 = split[0].indexOf("pri");	
				var index3 = split[0].indexOf("docs.count");
				var index4 = split[0].indexOf("docs.deleted");
				var index5 = split[0].indexOf("store.size");
				var index6 = split[0].indexOf("pri.store.size");
			
			//var col1,col2,col3,col4,col5,col6,col7;
				for(var i=1 ; i<split.length ; i++ ){
					if (split[i]) {
						col1 = split[i].substring(0, index1-1).trim();
						col2 = split[i].substring(index1, index2-1).replace(/\s+/g,"");
						col3 = split[i].substring(index2, index3-1).replace(/\s+/g,"");
						col4 = split[i].substring(index3, index4-1).replace(/\s+/g,"");
						col5 = split[i].substring(index4, index5-1).replace(/\s+/g,"");
						col6 = split[i].substring(index5, index6-1).replace(/\s+/g,"");
						col7 = split[i].substring(index6, split[i].length-1).trim();
					
					/**因store.size长度不同，临时处理**/
					if(col3.indexOf(" ") != -1){
						col3 = col3.split(" ")[0];
					}
					
					if(!col6){
						col6 = "0b";
					}
					if(!col7){
						col7 = "0b";
					}
					rr1.push({
						"health":col1,
					    "index":col2,
					    "pri":col3,
					    "count":col4,
					    "deleted":col5,
					    "size":col6,
					    "prisize":col7
						
					});
					}
			 	}
				rr = rr1;
				rr1 = null;
				var num = ((split.length-1)/$scope.cluster.indices_pagination.pageSize).toString();
				if(num.indexOf(".") == -1){
					$scope.cluster.indices_pagination.totalNum = parseInt(num);
				}else{
					$scope.cluster.indices_pagination.totalNum = parseInt(num.substring(0,num.indexOf(".")))+1;
				}
				$scope.cluster.indices_pagination.pages = [];
				for(var i=1;i<=$scope.cluster.indices_pagination.totalNum;i++){
					$scope.cluster.indices_pagination.pages.push(i);
				}
				$scope.showIndicesList();
		    
			});
		}
		$scope.showIndicesList = function(currentPage){
			$scope.cluster.indicesList = [];
			if(currentPage){
				$scope.cluster.indices_pagination.currentPage = currentPage;
			}
			var from = ($scope.cluster.indices_pagination.currentPage-1)*$scope.cluster.indices_pagination.pageSize;
			var to = $scope.cluster.indices_pagination.currentPage*$scope.cluster.indices_pagination.pageSize;
			//var usedStore;
			var n = 0;
			for(var doc in rr){
				if(n >= from && n < to){
					$scope.cluster.indicesList.push(rr[doc]);
				}
				//rr[doc].size += usedStore;
				n ++;
			}
			//$scope.cluster.usedstore=usedStore;
		};
		var waitter = {
				show:function(){
					var content = "<center style='margin-top:150px;'><i class=\"fa fa-spinner fa-spin fa-3x\"></i></center>";
					showLayers(content);
				},
				hidden:function(){
					$scope.hideLayers();
				}
			}

	    showLayers = function(content,bgcolor){
				$("#search_popup_layers").css("display","block");
				$("#search_popup_layers").css("position","absolute");
				$("#search_popup_layers").css("width",$("#view_main").css("width"));
				$("#search_popup_layers").css("height",$(document).height());
				$("#search_popup_layers").css("z-index","10");
				if(bgcolor){
					$("#search_popup_layers").css("background",bgcolor);
				}else{
					$("#search_popup_layers").css("background","rgba(69,69,69,0.2)");
				}
				$("#search_popup_layers").html(content);
			}
		$scope.toggle = function(id,e){
			//var buttonId = e.target.getAttribute('id');
			if ($('#show_'+id).is(":hidden")) {
				$(".dropdown-menu").hide();
				$('#show_'+id).slideDown();
				e.stopPropagation();
				//$('#'+buttonId).html('收起列表<span class="caret"></span>');
				//tag.html('收起列表<span class="caret"></span>');
			} else {
				$('#show_'+id).slideUp();
				/*$('#button_toggle').html('状态信息<span class="caret"></span>');*/
				//$('#'+buttonId).html('状态信息<span class="caret"></span>');
			}
		}
		$(".dropdown-menu").click(function(e) {
			e?e.stopPropagation():event.cancelBubble = true;
		}); 
		$scope.oprationIndex = function(id,e){
			if($("#opration_"+id).is(":hidden")){
				$(".dropdown-menu").hide();
				$("#opration_"+id).slideDown();
				e.stopPropagation();
			}else{
				$("#opration_"+id).slideUp();
			}
		}
		$scope.showIndexStatistics = function(stateName,index,id){
			$http.get(hs_es.host_name + "/" + index + "/_stats/"+stateName).success(function(data){
				var indexStatistics = data.indices[index].primaries;
				$scope.indexStatistics = indexStatistics;
				if("fielddata" == stateName){
					$scope.indexStatistics.fielddata.memory_size_in_bytes = dataformart(indexStatistics.fielddata.memory_size_in_bytes);
				}
				$('.dropdown-menu').hide();
				//$("#"+index).hide();
				//$("#"+id).html('状态信息<span class="caret"></span>');
				$("#"+stateName).show();
				
			})
		}
		$scope.closeIndex = function(index){
			$http.post(hs_es.host_name + "/" + index + "/_close" ).success(function(data){
				$scope.refresh();
			})
		}
		$scope.showCloseIndex = function(){
			$http.get(hs_es.host_name + "/_cluster/state").success(function(data){
				var indices = data.blocks.indices;
				$scope.indices = indices;
				//$('.dropdown-menu').hide();
				$("#closedIndex").show();
			})
		}
		$scope.openIndex = function(index){
			$http.post(hs_es.host_name + "/" + index +"/_open").success(function(data){
				$(".dropdown-menu").hide();
				$scope.refresh();
			})
		}
		$scope.optimizeIndex = function(e,index){
			//alert("22");
			$('.dropdown-menu').hide();
			$("#optimize_index").html(index);
			$("#optimizeIndex").show();
			e.stopPropagation();
		}
		$scope.optimize = function(){
			var index = $("#optimize_index").text();
			var segments = $("#segments").val();
			var ar = $("#checkbox :checked");
			var url = hs_es.host_name + "/" + index + "/_optimize?max_num_segments="+segments  
			if(ar.length != 0){
				for(var i=0;i<ar.length;i++){
					url += "&" + ar[i].name +"=true"
				}
			}
			console.log(url);
			$http.post(url).success(function(data){
			})
			$(".dropdown-menu").hide();			
		}
		$scope.deleteIndex = function(index){
			/*function getXMLHTTPRequest(){  
			    if (XMLHttpRequest)    {  
			        return new XMLHttpRequest();  
			    } else {  
			        try{  
			            return new ActiveXObject('Msxml2.XMLHTTP');  
			        }catch(e){  
			            return new ActiveXObject('Microsoft.XMLHTTP');  
			        }  
			    }  
			}   
			var req = getXMLHTTPRequest();  
			req.open('DELETE',hs_es.host_name + "/" + index , false);   
			req.send(null);*/
			if(confirm("确定要删除该索引么？")){
				$http({
					 url:hs_es.host_name + "/" + index,
				     method:"DELETE",
				     data:null
				}).success(function(data){
					//console.log(data);
					$scope.refresh();
				})
			}
			
		}
		$scope.close = function(){
			$(".dropdown-menu").hide();
		};
		$scope.hideLayers = function(){
			$("#search_popup_layers").css("display","none");
		}
		$scope.$on('$viewContentLoaded', function() {  
			waitter.show();
	    });  
		/**节点详情点击事件**/
		$scope.showNodeDetail = function (key){
			$rootScope.app_nav = "首页 / 集群监控 / 节点详情";
			location = "#/clusterDetail/" + key;
		};
		/**index详情点击事件**/
		$scope.showIndexDetail = function (key){
			$rootScope.app_nav = "首页 / 集群监控 / index详情";
			location = "#/indexDetail/" + key;
		};
		
	    /*$(function(){
			showNodes();
		});*/
	    showNodes();
		showIndeces();
		
		$scope.refresh = function(){
			$scope.timeout();
			showNodes();
			showIndeces();
			waitter.hidden();
		}
	}]);
});