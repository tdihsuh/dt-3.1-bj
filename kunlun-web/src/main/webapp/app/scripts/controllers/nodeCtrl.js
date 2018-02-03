define(['app'],function(app){
	app.controller("nodeCtrl",["$scope", "$http", 'hs_host', '$timeout', function($scope, $http, hs_host, $timeout){
		/**获取集群概要信息**/
		$http.get(hs_host + "/_cluster/stats").success(function(data){
			$scope.cluster = data;
		});
		/**获取文档及索引**/
		$http.get(hs_host + "/_stats").success(function(data){
			var docList = {};
			for(var doc in data.indices){
				if(doc.indexOf(".")){
					docList[doc] = data.indices[doc];
				}
			}
			$scope.docs = docList;
		});
		
		/**获取节点信息**/
		$scope.nodes = {};
		$scope.cluster1 = {
				"pagination":{
					"currentPage":1,
					"pageSize":2,
					"pageNum":0
				}
		};
		$scope.nodes.pageSizeMap = [];
		for(var i = 1; i <= $scope.cluster1.pagination.pageSize; i++){
			$scope.nodes.pageSizeMap.push(i);
		}
		var nodesUrl = hs_host + "/_nodes/stats";
		var nodeList = {};
		$http.get(nodesUrl).success(function(data){
			var reg = /[\w\W]*\[[\w\W]*\/([0-9\.]*):.*/;
			for(var node in data.nodes){
				nodeList[node] = data.nodes[node];
				var tmp = nodeList[node].ip[0].match(reg);
				if (tmp) nodeList[node].ip[0] = tmp[1];
			}
			$scope.nodeList = nodeList;
			$scope.showNodes(1);
		});
		$scope.showNodes = function(currentPage){
			$scope.cluster1.nodesList = [];
			if(currentPage){
				$scope.cluster1.pagination.currentPage = currentPage;
			}
			var from = ($scope.cluster1.pagination.currentPage-1)*$scope.cluster1.pagination.pageSize;
			var to = $scope.cluster1.pagination.currentPage*$scope.cluster1.pagination.pageSize;
			var n = 0;
			for(var node in nodeList){
				if(n >= from && n < to){
					$scope.cluster1.nodesList.push(nodeList[node]);
				}
				n ++;
			}
		};
		
		/*
		$scope.showNodes = function(currentPage){
			$scope.nodes.currentPage = currentPage;
			console.log($scope.nodes.currentPage);
		};*/
		
		/**节点详情点击事件**/
		$scope.showNodeDetail = function (key){
			console.info(key);
			location = "#/detail/" + key;
		};
		
		/**cpu**/
		$(function () {
		    $(document).ready(function() {
		    	/**定时刷新数据**/
		    	var timerId;
		    	var osuseage = 0;
				var jvmuseage = 0;
				var str = "master";
				$scope.timeout = function(){
					timerId = $timeout(function(){
						$http.get(hs_host + "/_nodes/stats/os").success(function(data){
							
							for(var os in data.nodes){
								if(str.indexOf(data.nodes[os].transport_address)){
									osuseage = data.nodes[os].os.cpu.usage;
								}
							}
	        			}); 
						$http.get(hs_host + "/_nodes/stats/jvm").success(function(data){
							for(var os in data.nodes){
								if(str.indexOf(data.nodes[os].transport_address)){
									jvmuseage = data.nodes[os].jvm.mem.heap_used_percent;
								}
							}
	        			}); 
						$scope.timeout();
					},1000);
				};
				$scope.$on('$destroy', function() {
		            $timeout.cancel(timerId);
		        });
				$scope.timeout();
				
		       /* Highcharts.setOptions({                                                     
		            global: {                                                               
		                useUTC: false                                                       
		            }                                                                       
		        }); */ 

		        $('#container').highcharts({                                                
		            chart: {                                                                
		                type: 'spline',                                                     
		                animation: Highcharts.svg, // don't animate in old IE               
		                marginRight: 10,                                                    
		                events: {                                                           
		                    load: function() {                                              
		                        // set up the updating of the chart each second             
		                        var series = this.series[0];                                
		                        setInterval(function() {                                    
		                            var x = (new Date()).getTime(), // current time         
		                                y = osuseage;                                  
		                            series.addPoint([x, y], true, true);                    
		                        }, 1000);                                                   
		                    }                                                               
		                }                                                                   
		            },  
		            credits:{  
	                    enabled: true,  
	                    href: "http://www.hansight.com",  
	                    text: '瀚思安信'  
	                }, 
		            title: {                                                                
		                text: 'master节点CPU状态图'                                            
		            },                                                                      
		            xAxis: {                                                                
		                type: 'datetime',                                                   
		                tickPixelInterval: 150                                              
		            },                                                                      
		            yAxis: {                                                                
		                title: {                                                            
		                    text: 'cpu使用率（%）'                                                   
		                },                                                                  
		                plotLines: [{                                                       
		                    value: 0,                                                       
		                    width: 1,                                                       
		                    color: '#808080'                                                
		                }]                                                                  
		            },                                                                      
		            tooltip: {                                                              
		                formatter: function() {                                             
		                        return '<b>'+ this.series.name +'</b><br/>'+                
		                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
		                        Highcharts.numberFormat(this.y, 2);                         
		                }                                                                   
		            },                                                                      
		            legend: {                                                               
		                enabled: false                                                      
		            },                                                                      
		            exporting: {                                                            
		                enabled: false                                                      
		            },                                                                      
		            series: [{                                                              
		                name: 'CPU useage（%）',                                                
		                data: (function() {                                                 
		                    // generate an array of random data                             
		                    var data = [],                                                  
		                        time = (new Date()).getTime(),                              
		                        i;                                                          
		                                                                                    
		                    for (i = -19; i <= 0; i++) {                                    
		                        data.push({                                                 
		                            x: time + i * 1000,                                     
		                            y: 0                                  
		                        });                                                         
		                    }                                                               
		                    return data;                                                    
		                })()                                                                
		            }]                                                                      
		        });                                                                         
		       
		    
		    /**jvm**/
		    $('#container1').highcharts({                                                
	            chart: {                                                                
	                type: 'spline',                                                     
	                animation: Highcharts.svg, // don't animate in old IE               
	                marginRight: 10,                                                    
	                events: {                                                           
	                    load: function() {                                              
	                        // set up the updating of the chart each second             
	                        var series = this.series[0];                                
	                        setInterval(function() {                                    
	                            var x = (new Date()).getTime(), // current time         
	                                y = jvmuseage;                                  
	                            series.addPoint([x, y], true, true);                    
	                        }, 1000);                                                   
	                    }                                                               
	                }                                                                   
	            },  
	            credits:{  
                    enabled: true,  
                    href: "http://www.hansight.com",  
                    text: '瀚思安信'  
                }, 
	            title: {                                                                
	                text: 'master节点JVM状态图'                                            
	            },                                                                      
	            xAxis: {                                                                
	                type: 'datetime',                                                   
	                tickPixelInterval: 150                                              
	            },                                                                      
	            yAxis: {                                                                
	                title: {                                                            
	                    text: 'JVM使用率（%）'                                                   
	                },                                                                  
	                plotLines: [{                                                       
	                    value: 0,                                                       
	                    width: 1,                                                       
	                    color: '#808080'                                                
	                }]                                                                  
	            },                                                                      
	            tooltip: {                                                              
	                formatter: function() {                                             
	                        return '<b>'+ this.series.name +'</b><br/>'+                
	                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
	                        Highcharts.numberFormat(this.y, 2);                         
	                }                                                                   
	            },                                                                      
	            legend: {                                                               
	                enabled: false                                                      
	            },                                                                      
	            exporting: {                                                            
	                enabled: false                                                      
	            },                                                                      
	            series: [{                                                              
	                name: 'JVM使用率（%）',                                                
	                data: (function() {                                                 
	                    // generate an array of random data                             
	                    var data = [],                                                  
	                        time = (new Date()).getTime(),                              
	                        i;                                                          
	                                                                                    
	                    for (i = -19; i <= 0; i++) {                                    
	                        data.push({                                                 
	                            x: time + i * 1000,                                     
	                            y: 0                                  
	                        });                                                         
	                    }                                                               
	                    return data;                                                    
	                })()                                                                
	            }]                                                                      
	        });  
		                                                                                    
		}); 
		});
		
		
		$scope.showNodes1 = function(category, para, currentPage){
			$scope.selectedRow = null;
			
			var para = {};
			
			NodeItems.query(nodesUrl, para, currentPage).then(function(data){
				$scope.nodes = data;
				var nodePageNum = data.pageNum;
				var pages = [];
				var n = 0;
				var curr = data.currentPage;
				for(var i=curr-5;i<curr-1;i++){
					if(i >= 0)
						pages[n++] = i+1;
				}
				pages[n++] = curr;
				for(var i=curr+1;i<curr+5 && i <= nodePageNum;i++){
					pages[n++] = i;
				}
				
				$scope.nodePages = pages;
				$scope.category = category;
			});
		};
		//$scope.showNodes();
		/******************/
	}])
});