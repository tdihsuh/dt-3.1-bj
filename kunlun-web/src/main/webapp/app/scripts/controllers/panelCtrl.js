define(['app','scripts/factories/pieCharts','scripts/factories/barCharts','scripts/factories/panel/panelService','scripts/factories/panel/hits','../../../vendor/numeral','spectrum','scripts/factories/panel/sparklines','../../../vendor/lodash','scripts/factories/charts/stackedBarCharts','moment','scripts/factories/common/aliases'],
		function(app,pieCharts,BarCharts,PanelService,PanelHits,numeral,spectrum,PanelSparklines,_,StackedBarCharts,moment,Aliases){
	app.controller('panelCtrl',['$scope','$http','hs_color','$compile','hs_info','$resource','$filter','hs_es','$rootScope','BarCharts','$routeParams','PanelService','PanelHits','PieCharts','PanelSparklines','$timeout','StackedBarCharts','$q','Aliases'
	                            ,function($scope,$http,hs_color,$compile,hs_info,$resource,$filter,hs_es,$rootScope,BarCharts,$routeParams,PanelService,PanelHits,PieCharts,PanelSparklines,$timeout,StackedBarCharts,$q,Aliases){
		$scope.panel = {
			"type" : null,	//面板类型
			"columns" : null,	//面板列数（宽度）
			"column":{"type":{"key":null}},
			//"editEnable" : false,	//是否可编辑
			"aggs" : null,
			"style" : null,
			"stats":['50.0', '75.0', '95.0', '99.0'],
			"stats1":['count', 'min', 'max', 'mean', 'total', 'variance', 'std_deviation', 'sum_of_squares'],
			"statsCheck":[],
			"statsCheck1":[],
			"confirms" :[],
			"panels" : [],  //Column中存储各个panel数据容器
			//"confirms1" :[],
			"mode" :['html','markdown','text'],
			"form" : {
				"type":{"key":null},
				"height":280
			},
			"name" : "charts_",
			"properties_name" : "pro_",
			"requestUrl" : hs_es.host_name,
			"requestParam" : "",
			"panelId" : "panel_popup",
			"datas" : [],
			"saveId" : "save_panel_",
			"module" : {	//当前主要用于panel的保存功能
				"module":true,	//panel保存时不直接保存到数据库中，先在页面上显示
				"show" : false	//panel直接保存在数据库中，同时页面上显示
			},
			"timerId":[],
			"timeout":[]
		};
		$scope.initPanelModule = function(str){
			for(var ele in $scope.panel.module){
				$scope.panel.module[ele] = false;
			}
			$scope.panel.module[str] = true;
		};
		
		$http.get("app/data/panel.json").success(function(data){
			$scope.panel.type = data._type;
			$scope.panel.ColPaneltype = data._ColPaneltype;
			$scope.panel.columns = data._column;
			$scope.panel.aggs = data._aggs;
			$scope.panel.style = data._style;
			$scope.panel.fontsize = data._fontsize;
			$scope.panel.legend = data._legend;
			$scope.panel.sortterm = data._sortterm;
			$scope.panel.sort = data._sort;
			$scope.panel.aggs1 = data._aggs_1;
		});
		$scope.initText = function(){
			if($scope.panel.form.fontsize == undefined || $scope.panel.form.fontsize == "") {
				$scope.panel.form.fontsize = $scope.panel.fontsize.value[8];
			}
			$scope.panel.mode = "text";
		};
		$scope.initAggStats = function(){
			if($scope.panel.form.spyable==undefined){
				$scope.panel.form.spyable=true;
			}
			if ($scope.panel.form.unit == undefined) {
				$scope.panel.form.unit="毫秒";
			}
			if ($scope.panel.form.field == undefined||$scope.panel.form.field=="") {
				$scope.panel.form.field="";
			}
			if ($scope.panel.format == undefined || $scope.panel.format == "") {
				$scope.panel.format='number';
			}
			if ($scope.panel.label_name == undefined || $scope.panel.label_name == "") {
				$scope.panel.label_name="Query";
			}
			if($scope.panel.display_breakdown == undefined || $scope.panel.display_breakdown == "") {
				$scope.panel.display_breakdown="yes";
			}
			for(var t in $scope.panel.stats) {
				if ($scope.panel.stats[$scope.panel.stats[t]]== undefined || $scope.panel.stats[$scope.panel.stats[t]] == "") {
					$scope.panel.stats[$scope.panel.stats[t]]=true;
				} 

			}
			if($scope.panel.form.fontsize == undefined || $scope.panel.form.fontsize == "") {
				$scope.panel.form.fontsize = $scope.panel.fontsize.value[8];
			}
			
		}
		
		$scope.initStats = function(){
			if ($scope.panel.form.unit == undefined) {
				$scope.panel.form.unit="";
			}
			if($scope.panel.form.spyable==undefined){
				$scope.panel.form.spyable=true;
			}
			if ($scope.panel.form.field == undefined||$scope.panel.form.field=="") {
				$scope.panel.form.field="";
			}
			if ($scope.panel.format == undefined || $scope.panel.format == "") {
				$scope.panel.format='number';
			}
			if ($scope.panel.label_name == undefined || $scope.panel.label_name == "") {
				$scope.panel.label_name="Query";
			}
			if($scope.panel.display_breakdown == undefined || $scope.panel.display_breakdown == "") {
				$scope.panel.display_breakdown="yes";
			}
			for(t in $scope.panel.stats1) {
				if ($scope.panel.stats1[$scope.panel.stats1[t]]== undefined || $scope.panel.stats1[$scope.panel.stats1[t]] == "") {
					$scope.panel.stats1[$scope.panel.stats1[t]]=true;
				} 

			}
			if($scope.panel.form.fontsize == undefined || $scope.panel.form.fontsize == "") {
				$scope.panel.form.fontsize = $scope.panel.fontsize.value[8];
			}
		}
		
		
		/**设置SparkShell时间选择框**/
		$scope.initSparkShell = function() {
			$("#datepicker_from1").datepicker($.datepicker.regional["zh"]).datepicker("option","dateFormat","yy-mm-dd");
			$("#datepicker_to1").datepicker($.datepicker.regional["zh"]).datepicker("option","dateFormat","yy-mm-dd");
			var id=$scope.panel.form.id;
			if (id == '' || id == undefined) {
				$('#datepicker_from1').datepicker('setDate', -30);
				$('#datepicker_to1').datepicker('setDate', new Date());
				$scope.panel.form.starttime = $('#datepicker_from1').val();
				$scope.panel.form.endtime = $('#datepicker_to1').val();
			}
		};
		
		
		$scope.showCondition = function(){
			if($scope.panel.form.condition==''||$scope.panel.form.condition==undefined){
				alert("请先输入查询条件！");
				return;
			}
			$scope.panel.form.sparkconditionshowflag=1;
		};
		$scope.closeCondition = function(){
			$scope.panel.form.sparkconditionshowflag=0;
			$scope.panel.form.condition='';
		};
		/**agg_stats确认查询信息**/
		$scope.showAggStatsConfirm = function(){
			//var valCheck=document.panel_popup.agg_query.checkValidity();
			if($scope.panel.form.query==''||$scope.panel.form.query==undefined){
				alert("请先输入查询条件！");
				return;
			}
			var leg=parseInt($scope.panel.confirms.length);
			var i=0;
			var color="";
			if(leg!=0){
				i=leg;
			}
			if(leg%2==0){
				color="green";
			}else{
				color="red";
			}
			$scope.panel.confirms[i]={
				"color":color,
				"value":$scope.panel.form.query
			};
			console.log($scope.panel.confirms);
			$scope.panel.form.query="";
		};
		/**agg_stats删除查询信息**/
		$scope.delAggStatsConfirm = function(confirm){
			var leg=$scope.panel.confirms.length;
			for(var i=0;i<leg;i++ ){
				if($scope.panel.confirms[i].value==confirm){
					$scope.panel.confirms.splice(i,1);
				}
			}
			//console.log($scope.panel.confirms);
		};
		$scope.clickXAxis = function(){
			if($scope.panel.form.xAxis){
				$scope.panel.form.xAxis = false;
			}else{
				$scope.panel.form.xAxis = true;
			}
		};
		$scope.clickYAxis = function(){
			if($scope.panel.form.yAxis){
				$scope.panel.form.yAxis = false;
			}else{
				$scope.panel.form.yAxis = true;
			}
		};
		$scope.clickShowTitle = function(){
			if($scope.panel.form.showTitle){
				$scope.panel.form.showTitle = false;
			}else{
				$scope.panel.form.showTitle = true;
			}
		}
		$scope.removeColumnPanel = function(app){
			$scope.panel.panels=_.without($scope.panel.panels,app);
		};
		$scope.moveColumnPanel = function(index1,index2){
			var array=$scope.panel.panels;
			array.splice(index2, 0, array.splice(index1, 1)[0] );
			return array;
		};
		/** 向column中添加面板 **/
		$scope.addColumnPanel = function(type){
			if (type) {
				//column面板绑定属性
				var id = $scope.panel.form.id;
				var panelType = $scope.panel.form.type;
				//var panelType = $scope.panel.form.type.key;
				var column = $scope.panel.form.column;
				var title = $scope.panel.form.title;
				var columnType=$scope.panel.column.type.key;
				//根据column面板中选择要添加的Type向
				$scope.bindingPanelData("",columnType);
				console.log($scope.panel.panels);
				$scope.panel.form={
					"title":title,
					"column":column,
					"type":panelType
				};
			} 

			$scope.clearColumnProperties();
			
		}
		/** Column中绑定pannel数据 **/
		$scope.bindingPanelData = function(id,columnType){
			if(id){
				
			}else{
				//column中要添加的面板标题
				var columTypeTitle=$scope.panel.column.title;
				//text面板属性
				var comment=$scope.panel.form.comment;//文本内容
				var mode =$scope.panel.mode;//文本模式
				//agg_stats和stats
				var spyable=$scope.panel.form.spyable;
				var stats=$scope.panel.form.stats;
				var format=$scope.panel.format;
				var statsShow=$scope.panel.form.statsShow;
				var field=$scope.panel.form.field;
				var unit=$scope.panel.form.unit;
				var label_name=$scope.panel.label_name;
				var display_breakdown=$scope.panel.display_breakdown;
				var confirms=$scope.panel.confirms;
				if(!$scope.panel.panels){
					$scope.panel.panels=[];
				}
				
				if (columnType=='text') {//----------------Text------
					var fontsize = $scope.panel.form.fontsize.key;//字体
					var app={
						"title":columTypeTitle,
						"type":columnType,
						"hide":false,
						"mode":mode,
						"fontsize":fontsize,
						"comment":comment
					};
					$scope.panel.panels.push(app);
				} else if (columnType=='agg_stats'){//-----------agg_stats---
					if(confirms.length==0){
						$scope.panel.confirms[0]={
							"color":'green',
							"value":'*'
						};
					}
					//每次进来先清空下，若不清空需要判断是否有重复并且还要排序，麻烦
					$scope.panel.statsCheck=[];
					var leg=$scope.panel.stats.length;
					for(var i=0;i<leg;i++ ){
						var stat=$scope.panel.stats[i];
						if($scope.panel.stats[stat]==true){
							$scope.panel.statsCheck.push($scope.panel.stats[i]) ;
						}
					}
					var fontsize=$scope.panel.form.fontsize;
					var app={
						"title":columTypeTitle,
						"type":columnType,
						"hide":false,
						"format":format,
						"spyable":spyable,
						"statsShow":statsShow,
						"field":field,
						"unit":unit,
						"statsCheck":$scope.panel.statsCheck,
						"fontsize":fontsize,
						"label_name":label_name,
						"display_breakdown":display_breakdown,
						"confirms":confirms
					};
					$scope.panel.panels.push(app);
					
				} else if (columnType=='stats') {//--------------stats
					if(confirms.length==0){
						$scope.panel.confirms[0]={
							"color":'green',
							"value":'*'
						};
					}
					//每次进来先清空下，若不清空需要判断是否有重复并且还要排序，麻烦
					$scope.panel.statsCheck1=[];
					var leg=$scope.panel.stats1.length;
					for(var i=0;i<leg;i++ ){
						var stat=$scope.panel.stats1[i];
						if($scope.panel.stats1[stat]==true){
							$scope.panel.statsCheck1.push($scope.panel.stats1[i]) ;
						}
					}
					var fontsize=$scope.panel.form.fontsize;
					var app={
						"title":columTypeTitle,
						"type":columnType,
						"hide":false,
						"format":format,
						"spyable":spyable,
						"statsShow":statsShow,
						"field":field,
						"unit":unit,
						"statsCheck":$scope.panel.statsCheck1,
						"fontsize":fontsize,
						"label_name":label_name,
						"display_breakdown":display_breakdown,
						"confirms":confirms
					};
					$scope.panel.panels.push(app);
				} 
			}
		};
		
		$scope.addPanel = function(id){
			if(id){
				$scope.panel.form = {id:id};
				$scope.panel.form.height = 280;
				var obj = $("#"+id);
				var pro_type = obj.attr($scope.panel.properties_name+"type");
				var pro_content = eval("("+obj.attr($scope.panel.properties_name+"content")+")");
				
				//-------------Column---------------
				$scope.panel.form.id=id;
				$scope.panel.panels=pro_content.panels;
				if($scope.panel.properties_name+"panel"){
					$scope.panel.colpanel=eval("("+obj.attr($scope.panel.properties_name+"panel")+")");
				};
				
				
				var pro_title = pro_content.title;
				var pro_width = pro_content.width;
				var pro_style = pro_content.style;
				var pro_startime = pro_content.startime;
				var pro_endtime = pro_content.endtime;
				var pro_condition = pro_content.condition;
				var pro_fontsize = pro_content.fontsize;
				var pro_comment = pro_content.comment;
				
				var pro_spyable = pro_content.spyable;
				var pro_statsShow=pro_content.statsShow;
				var pro_field=pro_content.field;
				var pro_unit=pro_content.unit;
				var pro_statsCheck=pro_content.statsCheck;
				var pro_labelName=pro_content.label_name;
				var pro_displayBreakdown=pro_content.display_breakdown;
				var pro_confirms=pro_content.confirms;
				var pro_format=pro_content.format;
				
				var pro_mode = pro_content.mode;
				var pro_aggs_type = pro_content.aggsType;
				var pro_aggs_value = pro_content.aggsValue;
				
				var pro_xAxis = pro_content.xAxis;
				var pro_yAxis = pro_content.yAxis;
				var pro_color = pro_content.color;
				var pro_showTitle = pro_content.showTitle;
				
				var pro_sortterm = pro_content.sortterm;
				var pro_sort = pro_content.sort;
				var pro_legend = pro_content.legend;
				
				$scope.panel.mode = pro_mode;
				for(var t in $scope.panel.type.value){
					if($scope.panel.type.value[t].key == pro_type){
						$scope.panel.form.type = $scope.panel.type.value[t];
						//$scope.panel.column.type = $scope.panel.type.value[t];
					}
				}
				for(var c in $scope.panel.columns.value){
					if($scope.panel.columns.value[c].key == pro_width){
						$scope.panel.form.column = $scope.panel.columns.value[c];
					}
				}
				for(var a in $scope.panel.aggs.value){
					if($scope.panel.aggs.value[a].key == pro_aggs_type){
						$scope.panel.form.aggs = {type:$scope.panel.aggs.value[a]};
						if($scope.panel.aggs.value[a].key == "time"){
							for(var p in $scope.panel.aggs.value[a].properties){
								if($scope.panel.aggs.value[a].properties[p].key == pro_aggs_value){
									$scope.panel.form.aggs.value = $scope.panel.aggs.value[a].properties[p];
								}
							}
						}else if($scope.panel.aggs.value[a].key == "field"){
							$scope.panel.form.aggs.value = pro_aggs_value;
						}
					}
				}
				for(var s in $scope.panel.style.value){
					if($scope.panel.style.value[s].key == pro_style){
						$scope.panel.form.style = $scope.panel.style.value[s];
					}
				}
				
				for(var f in $scope.panel.fontsize.value){
					if (pro_fontsize) {
						if($scope.panel.fontsize.value[f].key == pro_fontsize){
							$scope.panel.form.fontsize = $scope.panel.fontsize.value[f];
						}
					} 
				}
				
				$scope.panel.form.title = pro_title;
				$scope.panel.form.starttime=pro_startime;
				$scope.panel.form.endtime=pro_endtime;
				$scope.panel.form.sparkconditionshowflag=1;
				$scope.panel.form.condition = pro_condition;
				$scope.panel.form.comment = pro_comment;
				
//				if(pro_edit_enable == 0)
//					$scope.panel.form.editEnable = false;
//				else
//					$scope.panel.form.editEnable = true;
				
				$scope.panel.form.xAxis = pro_xAxis;
				$scope.panel.form.yAxis = pro_yAxis;
				$scope.panel.form.color = pro_color;
				$scope.panel.form.showTitle = pro_showTitle;
				
				if(pro_content.height) $scope.panel.form.height = pro_content.height;
				for(var s in $scope.panel.sortterm.value){
					if($scope.panel.sortterm.value[s].key == pro_sortterm){
						$scope.panel.form.sortterm = $scope.panel.sortterm.value[s];
					}
				}
				for(var s in $scope.panel.sort.value){
					if($scope.panel.sort.value[s].key == pro_sort){
						$scope.panel.form.sort = $scope.panel.sort.value[s];
					}
				}
				for(var l in $scope.panel.legend.value){
					if($scope.panel.legend.value[l].key == pro_legend){
						$scope.panel.form.legend = $scope.panel.legend.value[l];
					}
				}
				for(var l in $scope.panel.statsCheck){
					for(var s in $scope.panel.stats){
						if ($scope.panel.statsCheck[l] == $scope.panel.stats[s]) {
							var stat=$scope.panel.stats[s];
							$scope.panel.stats[stat]=true;
						}
					}
				}
				
				$scope.panel.display_breakdown=pro_displayBreakdown;
				$scope.panel.confirms=pro_confirms;
				$scope.panel.format=pro_format;
				$scope.panel.form.spyable=pro_spyable;
				$scope.panel.form.field=pro_field;
				$scope.panel.form.unit=pro_unit;
				$scope.panel.label_name=pro_labelName;
				$scope.panel.form.statsShow=pro_statsShow;
				$scope.panel.form.refresh = pro_content.refresh;
				
				if(!$scope.panel.form.aggs1){
					$scope.panel.form.aggs1 = [];
				}
				if(!$scope.panel.form.aggs1.range){
					$scope.panel.form.aggs1.range = [];
				}
				$scope.panel.form.aggs1.range["value"] = pro_content.rangeValue;
				
				for(var a in $scope.panel.aggs1.value){
					if($scope.panel.aggs1.value[a].key == pro_content.aggsType){
						$scope.panel.form.aggs1["type"] = $scope.panel.aggs1.value[a];
					}
				}
				
				for(var p in $scope.panel.aggs.value[0].properties){
					if($scope.panel.aggs.value[0].properties[p].key == pro_content.rangeType){
						$scope.panel.form.aggs1.range.type = $scope.panel.aggs.value[0].properties[p];
					}
					if($scope.panel.aggs.value[0].properties[p].key == pro_content.rangeUnit){
						$scope.panel.form.aggs1.range.unit = $scope.panel.aggs.value[0].properties[p];
					}
				}
				$scope.panel.form.aggs1.field = pro_content.field;
				$scope.panel.form.aggs1.fieldValue = pro_content.fieldValue;
				$scope.panel.form.xRotate = pro_content.xRotate;
				$scope.panel.form.aggs.aggsField = pro_content.aggsField;
			}
			$scope.showLayers();
				//console.log($('input[name=title]').val());
		}
		$scope.savePanel = function(){
			var id = $scope.panel.form.id;
			var panelType = $scope.panel.form.type.key;
			var column = $scope.panel.form.column.key;
			var title = $scope.panel.form.title;
			var colType=$scope.panel.column.type.key;
			if (panelType=="histogram") {
				$scope.showHistogram(id,panelType,column,title);
			}else if (panelType=="text") {
				$scope.showText(id,panelType,column,title,null,null,true);
			}else if(panelType=="sparkshell"){
				$scope.showSparkShell(id,panelType,column,title);
			}else if(panelType == "hits"){
				$scope.showHits(id,panelType,column,title);
			}else if(panelType=="agg_stats"){
				$scope.showAggStats(id,panelType,column,title,null,null);
			}else if (panelType=="stats") {
				$scope.showStats(id,panelType,column,title);
			}else if(panelType == "sparklines"){
				$scope.showSparklines(id,panelType,column,title);
			}else if (panelType=="column") {
				if(colType){
				$scope.addColumnPanel(colType);	
				}else{
				$scope.showColumn(id,panelType,column,title);
				}
			}

		};
		/** Column **/
		$scope.showColumn = function(id, panelType, column, title){
			//所有在column中要显示的panel
			var panels=$scope.panel.panels;
			
			var str = "";
			var panel_id,save_panel_id;
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				obj = $("#" + panel_id); 
				obj.attr("style","width:"+column);
				//$scope.panel.requestParam = $scope.panel.datas[panel_id].requestParam;
				//$scope.StatsResdata = $scope.panel.datas[panel_id].StatsResdata;
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				obj.attr("style","width:"+column);
				$("#panel_content").append(obj);
				//$scope.panel.requestParam = angular.copy($rootScope.panel.param);
			}
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			if(!$scope.panel.module.show){
				str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' title='保存' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			}
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='hs_content panel_content'  >";
			str += "<div id='panel_col_content_"+n+"' style='margin:10px;background-color:#F0F0F0;'>"
			str += "</div>";
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"panels":panels
			};
			
			obj.html(str);
			//根据panels中的数据展示
			for(var i=0; i<panels.length; i++) {
				var type=panels[i].type;
				if (type=='text') {
					panels[i].columnId=panel_id;
					$scope.showText(panels[i].id,type,"100%",panels[i].title,"panel_col_content_"+n,panels[i],false);
				} else if(type=='agg_stats'){
					$scope.showAggStats(id,type,"100%",panels[i].title,"panel_col_content_"+n,panels[i]);
				}
			}
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			if($scope.panel.module.show){//直接保存到数据库
				var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
				$scope.saveToDB(id,$scope.panel.saveId+dbId);
			}
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$scope.panel.panels=[];
			$compile($("#charts_"+n).contents())($scope);
		}
		/** Stats **/
		$scope.showStats = function(id, panelType, column, title){
			var spyable=$scope.panel.form.spyable;
			var stats1=$scope.panel.form.stats1;
			var format=$scope.panel.format;
			var statsShow=$scope.panel.form.statsShow;
			var field=$scope.panel.form.field;
			var unit=$scope.panel.form.unit;
			//每次进来先清空下，若不清空需要判断是否有重复并且还要排序，麻烦
			$scope.panel.statsCheck1=[];
			var leg=$scope.panel.stats1.length;
			for(var i=0;i<leg;i++ ){
				var stat=$scope.panel.stats1[i];
				if($scope.panel.stats1[stat]==true){
					$scope.panel.statsCheck1.push($scope.panel.stats1[i]) ;
				}
			}
			var fontsize=$scope.panel.form.fontsize;
			var label_name=$scope.panel.label_name;
			var display_breakdown=$scope.panel.display_breakdown;
			var confirms=$scope.panel.confirms;
			if(confirms.length==0){
				$scope.panel.confirms[0]={
					"color":'green',
					"value":'*'
				};
			}
			
			var str = "";
			var panel_id,save_panel_id;
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				obj = $("#" + panel_id); 
				obj.attr("style","width:"+column);
				$scope.panel.requestParam = $scope.panel.datas[panel_id].requestParam;
				$scope.StatsResdata = $scope.panel.datas[panel_id].StatsResdata;
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				obj.attr("style","width:"+column);
				$("#panel_content").append(obj);
				$scope.panel.requestParam = angular.copy($rootScope.panel.param);
			}
			
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-spinner fa-spin' ng-show='StatsResdata.valueshow==false'></i>"
			if ($scope.panel.form.spyable) {
				str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
			}
			if(!$scope.panel.module.show){
				str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' ng-show='StatsResdata.valueshow!=null' title='保存' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			}
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='hs_content panel_content' style='height:320px;width:100%;overflow:auto;'>";
			str += "<style>";
			str += "table.stats-table th, table.stats-table td {text-align: right;}"
			str += "table.stats-table th:first-child,  table.stats-table td:first-child {text-align: left;}"
			str += "</style>"
    		str += "<h1 style='text-align: center;height:100; line-height: 1.5em;padding-top:10px ; font-size:"+fontsize.value+"'><strong>{{StatsResdata.valueshow|formatstats:panel.format}}</strong> <small style='font-size:.6em; line-height: 0;'>"+unit+"("+statsShow+")</small></h1>"
      		str += "<table ng-show=\"panel.display_breakdown== 'yes'\" cellspacing='0' class='table-hover table table-condensed stats-table' style='margin-top: 38px;'>";
    		str += "<tbody><thead><tr><th Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF;'>";
			str += "<a style='font-size:15px;'><strong>"+label_name+"</strong></a></th>";
      		str += "<th ng-repeat='stat in panel.statsCheck1' Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
        	str += "<a href='' style='font-size:15px;'><strong>{{stat}}</strong></a></th></tr></thead>";
          	str += "<tr ng-repeat='row in StatsResdata.rows' ng-show='StatsResdata.valueshow!=null' Bgcolor='#FFFFFF'>";
			str += "<td ><i class='fa fa-circle' ng-style='{color:row.color}'></i> {{row.name}}</td>";
			str += "<td ng-repeat='stat in panel.statsCheck1'>{{row.resvalue[stat]|formatstats:panel.format}}"+ unit+"</td>";
			str += "</tr>";
			str += "</tbody></table>";
			str += "</div>";
			str += "<div class='hs_bottom panel_bottom'></div>";
			
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"format":format,
				"spyable":spyable,
				"statsShow":statsShow,
				"field":field,
				"unit":unit,
				"statsCheck":$scope.panel.statsCheck1,
				"fontsize":fontsize,
				"label_name":label_name,
				"display_breakdown":display_breakdown,
				"confirms":confirms
			};
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			obj.html(str);
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			
			var param = [];
			for(i=0;i<confirms.length;i++){
				param[i] = {
                  "query_string": {
                   "query": confirms[i].value
                  }
                };
			}
			
			$scope.panel.requestParam.facets = {
			    "stats": {
			      "statistical":  {
			        "field": field
			      },
				  "facet_filter": {
			        "fquery": {
			          "query": {
			            "filtered": {
			              "query": {
			                "bool": {
							  "should":param
			                }
			              }
						 }
			          }
			        }
			      }
			    }
			};
			
			//替换
			for (i = 0; i < confirms.length; i++) {
				var jsonStr=$scope.panel.requestParam.facets;
				var prop="stats_"+i;
				var cvalue=confirms[i].value;
				$scope.initStatsReq(jsonStr,prop, cvalue,field);
			}
			console.log($scope.panel.requestUrl);	
			console.log($scope.panel.requestParam);
			$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam)
			};
							
			$scope.StatsResdata={
				"rows":[],
				"valueshow":null
			};
			
			$http.post($scope.panel.requestUrl+"/_search?ignore_unavailable=true",$scope.panel.requestParam).success(function(data){
				var datas = data.facets;
				for(var x in datas){
					var z=x.indexOf('_');
					if(z!=-1){
						var num=parseInt(x.substr(z+1));
						var resval=datas[x];
						var color="";
						if(num%2==0){
							color="green";
						}else{
							color="red";
						}
						$scope.panel.confirms[num].resvalue=datas[x];
						$scope.StatsResdata.rows[num]={
							"resvalue":resval,
							"color":color,
							"name":$scope.panel.confirms[num].value
						};
					}else{
						$scope.StatsResdata.valueshow=datas[x][statsShow];
						console.log(statsShow);
						console.log(datas[x][statsShow]);
					}
					
				}
				$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam),
					StatsResdata:angular.copy($scope.StatsResdata)
				};
				console.log($scope.StatsResdata);
				if($scope.panel.module.show){//直接保存到数据库
					var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
					$scope.saveToDB(id,$scope.panel.saveId+dbId);
				}
				$scope.panel.confirms=[];
			});
			
			//显示查询语句
			$scope.showPanelCodeInfo(panel_id);
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$compile($("#charts_"+n).contents())($scope);
		}
		/** AggStats **/
		$scope.showAggStats = function(id, panelType, column, title,columncontent,panel){
			
			var spyable=$scope.panel.form.spyable;
			var stats=$scope.panel.form.stats;
			var format=$scope.panel.format;
			var statsShow=$scope.panel.form.statsShow;
			var field=$scope.panel.form.field;
			var unit=$scope.panel.form.unit;
			//每次进来先清空下，若不清空需要判断是否有重复并且还要排序，麻烦
			$scope.panel.statsCheck=[];
			var leg=$scope.panel.stats.length;
			for(var i=0;i<leg;i++ ){
				var stat=$scope.panel.stats[i];
				if($scope.panel.stats[stat]==true){
					$scope.panel.statsCheck.push($scope.panel.stats[i]) ;
				}
			}
			var fontsize=$scope.panel.form.fontsize;
			var label_name=$scope.panel.label_name;
			var display_breakdown=$scope.panel.display_breakdown;
			var confirms=$scope.panel.confirms;
			if(confirms.length==0){
				$scope.panel.confirms[0]={
					"color":'green',
					"value":'*'
				};
			}
			
			var str = "";
			var panel_id,save_panel_id;
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				obj = $("#" + panel_id); 
				obj.attr("style","width:"+column);
				$scope.panel.requestParam = $scope.panel.datas[panel_id].requestParam;
				$scope.AggStatsResdata = $scope.panel.datas[panel_id].AggStatsResdata;
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				obj.attr("style","width:"+column);
				$("#panel_content").append(obj);
				$scope.panel.requestParam = angular.copy($rootScope.panel.param);
			}
			
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-spinner fa-spin' ng-show='AggStatsResdata.valueshow==null'></i>"
			if ($scope.panel.form.spyable) {
				str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
			}
			if(!$scope.panel.module.show){
				str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' ng-show='AggStatsResdata.valueshow!=null' title='保存' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			}
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='hs_content panel_content' style='height:320px;width:100%;overflow:auto;'>";
			str += "<style>";
			str += "table.stats-table th, table.stats-table td {text-align: right;}"
			str += "table.stats-table th:first-child,  table.stats-table td:first-child {text-align: left;}"
			str += "</style>"
    		str += "<h1 style='text-align: center;height:100; line-height: 1.5em;padding-top:10px ; font-size:"+fontsize.value+"'><strong>{{AggStatsResdata.valueshow|formatstats:panel.format}}</strong> <small style='font-size:.6em; line-height: 0;'>"+unit+"("+statsShow+")</small></h1>"
      		str += "<table ng-show=\"panel.display_breakdown== 'yes'\" cellspacing='0' class='table-hover table table-condensed stats-table' style='margin-top: 38px;'>";
    		str += "<tbody><thead><tr><th Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
			str += "<a style='font-size:15px;'><strong>"+label_name+"</strong></a></th>";
      		str += "<th ng-repeat='stat in panel.statsCheck' Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
        	str += "<a href='' style='font-size:15px;'><strong>{{stat}}</strong></a></th></tr></thead>";
          	str += "<tr ng-repeat='row in AggStatsResdata.rows' ng-show='AggStatsResdata.valueshow!=null' Bgcolor='#FFFFFF'>";
			str += "<td ><i class='fa fa-circle' ng-style='{color:row.color}'></i> {{row.name}}</td>";
			str += "<td ng-repeat='stat in panel.statsCheck'>{{row.resvalue[stat]|formatstats:panel.format}}"+ unit+"</td>";
			str += "</tr>";
			str += "</tbody></table>";
			str += "</div>";
			str += "<div class='hs_bottom panel_bottom'></div>";
			
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"format":format,
				"spyable":spyable,
				"statsShow":statsShow,
				"field":field,
				"unit":unit,
				"statsCheck":$scope.panel.statsCheck,
				"fontsize":fontsize,
				"label_name":label_name,
				"display_breakdown":display_breakdown,
				"confirms":confirms
			};
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			obj.html(str);
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			
			var param = [];
			for(i=0;i<confirms.length;i++){
				param[i] = {
                  "query_string": {
                   "query": confirms[i].value
                  }
                };
			}
			
			$scope.panel.requestParam.aggregations = {
			    "stats": {
			      "aggs": {
			        "stats_agg": {
			          "percentiles": {
			            "field": field,
			            "percents": [
			              "50.0",
			              "75.0",
			              "95.0",
			              "99.0"
			            ]
			          }
			        }
			      },
				  "filter": {
			        "fquery": {
			          "query": {
			            "filtered": {
			              "query": {
			                "bool": {
							  "should":param
			                }
			              }
						 }
			          }
			        }
			      }
			    }
			};
			//替换
			for (i = 0; i < confirms.length; i++) {
				var jsonStr=$scope.panel.requestParam.aggregations;
				var prop="stats_"+i;
				var cvalue=confirms[i].value;
				$scope.initAggStatsReq(jsonStr,prop, cvalue,field);
			}
			console.log($scope.panel.requestUrl);	
			console.log($scope.panel.requestParam);
			$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam)
			};
							
			$scope.AggStatsResdata={
				"rows":[],
				"valueshow":null
			};
			$http.post($scope.panel.requestUrl+"/_search?ignore_unavailable=true",$scope.panel.requestParam).success(function(data){
				var datas = data.aggregations;
				for(var x in datas){
					var z=x.indexOf('_');
					if(z!=-1){
						var num=parseInt(x.substr(z+1));
						var resval=datas[x].stats_agg.values;
						var color="";
						if(num%2==0){
							color="green";
						}else{
							color="red";
						}
						$scope.panel.confirms[num].resvalue=datas[x].stats_agg.values;
						$scope.AggStatsResdata.rows[num]={
							"resvalue":resval,
							"color":color,
							"name":$scope.panel.confirms[num].value
						};
					}else{
						$scope.AggStatsResdata.valueshow=datas[x].stats_agg.values[statsShow];
					}
					
				}
				$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam),
					AggStatsResdata:angular.copy($scope.AggStatsResdata)
				};
				console.log($scope.AggStatsResdata);
				if($scope.panel.module.show){//直接保存到数据库
					var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
					$scope.saveToDB(id,$scope.panel.saveId+dbId);
				}
				$scope.panel.confirms=[];
			});
			
			//显示查询语句
			$scope.showPanelCodeInfo(panel_id);
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$compile($("#charts_"+n).contents())($scope);
		};
		/** 初始化Stats查询参数 
		 * 
		 * @param {Object} jsonStr 要添加的json串
		 * @param {Object} prop 要添加的字段
		 * @param {Object} cvalue 对应已经确定的查询条件
		 * @param {Object} field  查询field
		 */
		$scope.initStatsReq = function(jsonStr,prop, cvalue,field){
			 jsonStr[prop] = {
			      "statistical": {
			        "field": field
			      },
			      "facet_filter": {
			        "fquery": {
			          "query": {
			            "filtered": {
			              "query": {
			                "bool": {
			                  "should": [
			                    {
			                      "query_string": {
			                        "query": cvalue
			                      }
			                    }
			                  ]
			                }
			              }
			            }
			          }
			        }
			      }
			 };
		};
		/** 初始化AggStats查询参数 
		 * 
		 * @param {Object} jsonStr 要添加的json串
		 * @param {Object} prop 要添加的字段
		 * @param {Object} cvalue 对应已经确定的查询条件
		 * @param {Object} field  查询field
		 */
		$scope.initAggStatsReq = function(jsonStr,prop, cvalue,field){
			 jsonStr[prop] = {
			      "aggs": {
			        "stats_agg": {
			          "percentiles": {
			            "field": field,
			            "percents": [
			              "50.0",
			              "75.0",
			              "95.0",
			              "99.0"
			            ]
			          }
			        }
			      },
			      "filter": {
			        "fquery": {
			          "query": {
			            "filtered": {
			              "query": {
			                "bool": {
			                  "should": [
			                    {
			                      "query_string": {
			                        "query": cvalue
			                      }
			                    }
			                  ]
			                }
			              }
			            }
			          }
			        }
			      }
			 };
		};
		/** SparkShell **/
		$scope.showSparkShell = function(id, panelType, column, title){
			var startime = $('#datepicker_from1').val();
			var endtime = $('#datepicker_to1').val();
			var condition=$scope.panel.form.condition;
			var sparkconditionshowflag=$scope.panel.form.sparkconditionshowflag;
			if(condition==''||condition==undefined){
				alert("请输入查询条件！");
				return;
				}	
			if(sparkconditionshowflag==''||sparkconditionshowflag==undefined||sparkconditionshowflag==0){
				alert("请先确认查询条件！");
				return;
				}	
			var str = "";
			var panel_id,save_panel_id;
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				obj = $("#" + panel_id); 
				obj.attr("style","width:"+column);
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				obj.attr("style","width:"+column);
				$("#panel_content").append(obj);
			}
			
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' title='保存到仪表盘' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div ng-include=\"'app/template/panel/sparkshellshow.html'\">"
			str += "</div>";
			str += "<div class='hs_bottom panel_bottom'></div>";
			
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"startime":startime,
				"endtime":endtime,
				"condition":condition
			};
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			
			obj.html(str);
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			
			if($scope.panel.module.show){//直接保存到数据库
				var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
				$scope.saveToDB(id,$scope.panel.saveId+dbId);
			}
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$compile($("#charts_"+n).contents())($scope);
		};
		/** TextPanel **/
		$scope.showText=function(id,panelType,column,title,columncontent,panel,editIf){
			var fontsize,comment,mode,hide;
			if (!panel) {
				 fontsize = $scope.panel.form.fontsize.key;//字体
				 comment=$scope.panel.form.comment;//文本内容
				 mode =$scope.panel.mode;//文本内容
				 panel=$scope.panel.colpanel;
			}else{
				fontsize = panel.fontsize;//字体
			 	comment=panel.comment;//文本内容
			 	mode =panel.mode;//文本内容
			 	hide =panel.hide;//文本内容
			}
			
			var str = "";
			var panel_id,save_panel_id;
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				
				if (panel && !columncontent) {
					//obj = $("<div id='"+panel_id+"' ng-show='"+!panel.hide+"'></div>");
					obj = $("#" + panel_id);
					obj.attr("style","padding:5px");
					//$("#"+columncontent).append(obj);
				}else if(panel && columncontent){
					obj = $("<div id='"+$scope.panel.name+n+"' ng-show='"+!panel.hide+"'></div>");
					obj.attr("style","padding:5px");
					$("#"+columncontent).append(obj);
				}else{
					obj = $("#" + panel_id); 
					obj.attr("style","width:"+column);
				}
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				if (panel) {
					obj = $("<div id='"+$scope.panel.name+n+"' ng-show='"+!panel.hide+"'></div>");
					obj.attr("style","padding:5px");
					$("#"+columncontent).append(obj);
				} else {
					obj = $("<div id='"+$scope.panel.name+n+"'></div>");
					obj.attr("style","width:"+column);
					$("#panel_content").append(obj);
				}
			}
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			if(!$scope.panel.module.show && !panel){
				str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' title='保存' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			}
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='hs_content panel_content' style='word-break:break-all;width:100%;overflow:auto;font-size:"+fontsize+"pt'>";
			str += "<p>"+comment+"</p>"
			str += "</div>";
			str += "<div class='hs_bottom panel_bottom'></div>";
			
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"mode":mode,
				"fontsize":fontsize,
				"comment":comment
			};
			if (panel) {
				panel.id=panel_id;
//				panel.title=title;
//				panel.fontsize=fontsize  ;//字体
//				panel.comment=comment;//文本内容
//				panel.mode=mode ;//文本内容
				obj.attr($scope.panel.properties_name+"panel",JSON.stringify(panel));
			}
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			obj.html(str);
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			if($scope.panel.module.show){//直接保存到数据库
				if(!panel){
					var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
					$scope.saveToDB(id,$scope.panel.saveId+dbId);
				}else if(panel && !columncontent){//如果在column内部要更新column的content并且保存
					var dbId = $("#"+panel.columnId).attr($scope.panel.properties_name+"id");
					var type = $("#"+panel.columnId).attr($scope.panel.properties_name+"type");
					var content = eval("("+$("#"+panel.columnId).attr($scope.panel.properties_name+"content")+")");
					var panels=content.panels;
					for(var i=0 ;i< panels.length;i++){
						if(panels[i].id=panel_id){
							panels[i].title=title;
							panels[i].fontsize=fontsize;
							panels[i].comment=comment;
							panels[i].mode=mode;
							break;
						}
					}
					var content = {
						"title":content.title,
						"width":content.width,
						"panels":panels
					};
					$("#"+panel.columnId).attr($scope.panel.properties_name+"content",JSON.stringify(content));
					$scope.saveToDB(panel.columnId,$scope.panel.saveId+dbId);
				}
			}
			if(panel){
				$("#panel_content_"+n).attr("style","word-break:break-all;width:100%;height:"+panel.height+";overflow:auto;font-size:"+fontsize+"pt");
			} 
			if (!columncontent) {
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			}
			$compile($("#charts_"+n).contents())($scope);

		};
		
		$scope.showHits = function(id,panelType,column,title){
			var fontsize = 32;
			if($scope.panel.form.fontsize)
				fontsize = $scope.panel.form.fontsize.key;
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"fontsize":fontsize,
				"height":$scope.panel.form.height
			};
			
			var id_num = $scope.buildPanel(id,panelType,content);
			
			delete $scope.panel.requestParam["from"];
			delete $scope.panel.requestParam["size"];
			delete $scope.panel.requestParam["sort"];
			
			$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam)
			};
			PanelHits.generate($scope.panel.requestUrl+ "/" + $rootScope.panel.indices +"/_count",$scope.panel.requestParam,id_num,content);
			if($scope.panel.module.show){//直接保存到数据库
				var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
				$scope.saveToDB(id,$scope.panel.saveId+dbId);
			}
			//显示查询语句
			$scope.showPanelCodeInfo(panel_id,"_count");
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$compile($("#charts_"+id_num).contents())($scope);
		};
		
		$scope.panel.sparkline = [];
		
		$scope.showSparklines = function(id,panelType,column,title){
			var color = null;
			var legend = "none";
			if($("#colorpicker").spectrum("get") != null)
				color = $("#colorpicker").spectrum("get").toHexString();
			if($scope.panel.form.legend)
				legend = $scope.panel.form.legend.key;
			var content = {
				"title":title,
				"width":column,
				"height":$scope.panel.form.height,
				"xAxis":$scope.panel.form.xAxis,
				"yAxis":$scope.panel.form.yAxis,
				"showTitle":$scope.panel.form.showTitle,
				"color":color,
				"legend":legend,
				"field":$scope.panel.form.aggs1.field,
				"fieldValue":$scope.panel.form.aggs1.fieldValue,
				"aggsType":$scope.panel.form.aggs1.type.key,
				"aggsValue":$scope.panel.form.aggs1.value,
				"refresh":$scope.panel.form.refresh,
				"rangeType":$scope.panel.form.aggs1.range.type.key,
				"rangeValue":$scope.panel.form.aggs1.range.value,
				"rangeUnit":$scope.panel.form.aggs1.range.unit.key,
				"xRotate":$scope.panel.form.xRotate
			}
			var id_num = $scope.buildPanel(id,panelType,content);
			var skey = "sparkline_"+id_num;
			/***定时器***/
			var b = false;
			for(var s in $scope.panel.sparkline){
				if(s == id_num){
					b = true;
					break;
				}
			}
			if(!b){
				$scope.panel.sparkline[skey] = $scope.panel.requestParam;
			}
			switch (content.rangeUnit) {
				case 'second':
					$scope.panel.refresh = content.refresh;
					break;
				case 'minute':
					$scope.panel.refresh = content.refresh*60;
					break;
				case 'hour':
					$scope.panel.refresh = content.refresh*60*60;
					break;
				case 'day':
					$scope.panel.refresh = content.refresh*60*60*24;
					break;
			}
//			$scope.panel.timeout["timeout_"+id_num] = function(){
//				if($scope.panel.refresh){
//					$scope.panel.timerId["time_id_"+id_num] = $timeout(function(){
//						var to = new Date();
//						var from = hsTimes.getBeforeDateBySpecify(content.rangeType,content.rangeValue,to);
//						Aliases.getIndices($scope.expand_range(from,to,"day")).then(function(indices){
//							PanelSparklines.generate($scope.panel.requestUrl + "/" + indices +"/_search",$scope.panel.sparkline[skey],id_num,content,from,to);
//							$scope.panel.timeout["timeout_"+id_num]();
//						});
//					},$scope.panel.refresh);
//				}
//			};
//			$scope.panel.timeout["timeout_"+id_num]();
//			$scope.$on('$destroy', function() {
//				$timeout.cancel($scope.panel.timerId["time_id_"+id_num]);
//			});
			/***定时器结束***/
			
			$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam)
			};
			var to_ = new Date();
			var from_ = hsTimes.getBeforeDateBySpecify(content.rangeType,content.rangeValue,to_);
			console.log(from_);
			console.log(to_);
			Aliases.getIndices($scope.expand_range(from_,to_,"day")).then(function(indices){
				console.log(indices);
				PanelSparklines.generate($scope.panel.requestUrl + "/" + indices +"/_search?ignore_unavailable=true",$scope.panel.sparkline[skey],id_num,content,from_,to_);
				if($scope.panel.module.show){//直接保存到数据库
					var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
					$scope.saveToDB(id,$scope.panel.saveId+dbId);
				}
			});
			//显示查询语句
			$scope.showPanelCodeInfo(panel_id,"_count");
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$compile($("#charts_"+id_num).contents())($scope);
		}
		
		$scope.buildPanel = function(id,panelType,content){
			var n = null;
			if(id){//修改
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				panel_id = id;
				obj = $("#" + panel_id); 
				$scope.panel.requestParam = $scope.panel.datas[panel_id].requestParam;
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				$("#panel_content").append(obj);
				$scope.panel.requestParam = angular.copy($rootScope.panel.param);
			}
			var properties = {
				"panelId":panel_id,
				"panelType":panelType,
				"content":content,
				"save":!$scope.panel.module.show
			}
			PanelService.buildPanel(obj,properties);
			return n;
		}
		
		/** HistogramPanel **/
		$scope.showHistogram=function(id,panelType,column,title){
			
			var style = $scope.panel.form.style.key;
			var aggsType = $scope.panel.form.aggs.type.key;
			var aggsValue = null;
			var sortterm = "count";
			var sort = "desc";
			var legend = "none";
			var aggsField = null;
			if(aggsType == 'time')
				aggsValue = $scope.panel.form.aggs.value.key;
			else if(aggsType == 'field')
				aggsValue = $scope.panel.form.aggs.value;
			if($scope.panel.form.sortterm)
				sortterm = $scope.panel.form.sortterm.key;
			if($scope.panel.form.sort)
				sort = $scope.panel.form.sort.key;
			if($scope.panel.form.legend)
				legend = $scope.panel.form.legend.key;
			if($scope.panel.form.aggs.aggsField)
				aggsField = $scope.panel.form.aggs.aggsField;
			
			var str = "";
			
			var panel_id,save_panel_id;
			
			var obj;
			var n;
			
			if(id){//修改
				panel_id = id;
				n = id.substring(id.lastIndexOf("_")+1,id.length);
				save_panel_id = $scope.panel.saveId+n;
				obj = $("#" + panel_id); 
				obj.attr("style","width:"+column);
				$scope.panel.requestParam = $scope.panel.datas[panel_id].requestParam;
			}else{//增加
				$rootScope.panel.virtualNum ++;
				n = $rootScope.panel.virtualNum;
				
				panel_id = $scope.panel.name+n;
				save_panel_id = $scope.panel.saveId+n;
				obj = $("<div id='"+$scope.panel.name+n+"'></div>");
				obj.attr("style","width:"+column);
				$("#panel_content").append(obj);
				$scope.panel.requestParam = angular.copy($rootScope.panel.param);
			}
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+title+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
			if(!$scope.panel.module.show){
				str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' title='保存' ng-click='saveToDB(\""+panel_id+"\",\""+save_panel_id+"\")'></i>";
			}
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panel_id+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' onclick='$(\"#"+panel_id+"\").remove();'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='hs_content panel_content' style='height:"+$scope.panel.form.height+"px'></div>";
			str += "<div class='hs_bottom panel_bottom'></div>";
			
			var color = null;
			var legend = "none";
			if($("#colorpicker").spectrum("get") != null)
				color = $("#colorpicker").spectrum("get").toHexString();
			if($scope.panel.form.legend)
				legend = $scope.panel.form.legend.key;
			/***记录每个panel的属性及值**/
			var content = {
				"title":title,
				"width":column,
				"height":$scope.panel.form.height,
				"style":style,
				"aggsType":aggsType,
				"aggsValue":aggsValue,
				"xAxis":$scope.panel.form.xAxis,
				"yAxis":$scope.panel.form.yAxis,
				"showTitle":$scope.panel.form.showTitle,
				"color":color,
				"sortterm":sortterm,
				"sort":sort,
				"legend":legend,
				"aggsField":aggsField,
				"xRotate":$scope.panel.form.xRotate
			};
			obj.attr($scope.panel.properties_name+"id",n);
			obj.attr($scope.panel.properties_name+"type",panelType);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			
			
			obj.html(str);
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			
			delete $scope.panel.requestParam.from;
			$scope.panel.requestParam.size=0;
			if(aggsType == 'time'){
				if(style == "stacked_bar"){
					$scope.panel.requestParam.aggs = {
						"aggs":{
						      "terms":{
						        "field":aggsField+".raw"
						      },"aggs":{
						          "times":{
						            "date_histogram":{
						              "field":"@timestamp",
						              "interval":aggsValue
						            }
						          }
						      }
						 }
					}
				}else{
					$scope.panel.requestParam.aggs = {
							"aggs": {
								"date_histogram": {
									"field": "@timestamp",
									"interval": aggsValue
								}
							}
					}
				}
			}else if(aggsType == 'field'){
				if(style == "stacked_bar"){
					$scope.panel.requestParam.aggs = {
						"aggs":{
						      "terms":{
						        "field":aggsValue+".raw"
						      },"aggs":{
						          "times":{
						            "terms":{
						              "field":aggsField+".raw"
						            }
						          }
						      }
						 }
					}
				}else{
					$scope.panel.requestParam.aggs = {
						"aggs":{
							"terms":{
								"field":aggsValue+".raw"
							}
						}
					}
				}
			}
			$scope.panel.datas[panel_id] = {
					requestUrl:angular.copy($scope.panel.requestUrl),
					requestParam:angular.copy($scope.panel.requestParam)
			};
			$http.post($scope.panel.requestUrl + "/" + $rootScope.panel.indices +"/_search?ignore_unavailable=true",$scope.panel.requestParam).success(function(data){
				var datas = data.aggregations.aggs.buckets;
				
				if(datas.length == 0){
					if(aggsType == 'time'){
						if(style == "stacked_bar"){
							$scope.panel.requestParam.aggs = {
								"aggs":{
								      "terms":{
								        "field":aggsField
								      },"aggs":{
								          "times":{
								            "date_histogram":{
								              "field":"@timestamp",
								              "interval":aggsValue
								            }
								          }
								      }
								 }
							}
						}else{
							$scope.panel.requestParam.aggs = {
									"aggs": {
										"date_histogram": {
											"field": "@timestamp",
											"interval": aggsValue
										}
									}
							}
						}
					}else if(aggsType == 'field'){
						if(style == "stacked_bar"){
							$scope.panel.requestParam.aggs = {
								"aggs":{
								      "terms":{
								        "field":aggsValue
								      },"aggs":{
								          "times":{
								            "terms":{
								              "field":aggsField
								            }
								          }
								      }
								 }
							}
						}else{
							$scope.panel.requestParam.aggs = {
								"aggs":{
									"terms":{
										"field":aggsValue
									}
								}
							}
						}
					}
					$scope.panel.datas[panel_id] = {
							requestUrl:angular.copy($scope.panel.requestUrl),
							requestParam:angular.copy($scope.panel.requestParam)
					};
					$http.post($scope.panel.requestUrl+ "/" + $rootScope.panel.indices +"/_search?ignore_unavailable=true",$scope.panel.requestParam).success(function(data){
						var datas = data.aggregations.aggs.buckets;
						if(style == "stacked_bar")
							$scope.generateStackedBarCharts(datas,style,n,content);
						else
							$scope.generateCharts(datas,style,n,content);
						if($scope.panel.module.show){//直接保存到数据库
							var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
							$scope.saveToDB(id,$scope.panel.saveId+dbId);
						}
					});
				}else{
					if(style == "stacked_bar")
						$scope.generateStackedBarCharts(datas,style,n,content);
					else
						$scope.generateCharts(datas,style,n,content);
					if($scope.panel.module.show){//直接保存到数据库
						var dbId = $("#"+id).attr($scope.panel.properties_name+"id");
						$scope.saveToDB(id,$scope.panel.saveId+dbId);
					}
				}
			});
			//显示查询语句
			$scope.showPanelCodeInfo(panel_id);
			
			$scope.hideLayers();
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$compile($("#charts_"+n).contents())($scope);
		};
		$scope.showPanelCodeInfo = function(panel_id,searchWay){
			if(!searchWay) searchWay = "_search?pretty"
			var str = "curl -XGET";
			str += " '"+$scope.panel.datas[panel_id].requestUrl+"/"+searchWay+"' -d<br/>";
			str += " '" + JSON.stringify($scope.panel.datas[panel_id].requestParam)+"'";
			$("#"+panel_id+" .panel_code").attr("title",str);

			$("#"+panel_id+" .panel_code").tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:'bottom'
			});			
		}
		$scope.generateStackedBarCharts = function(datas,style,n,content){
			var ds = new Array();
			var key,count;
			var arr = [];
			var eles;
			for(var i in datas){
				key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
				eles = datas[i].times.buckets;
				for(var j in eles){
					var e_key = !!eles[j].key_as_string ? eles[j].key_as_string :eles[j].key;
					if(content.aggsType == 'time')
						e_key = $filter('date')(e_key,"yyyy-MM-dd HH:mm:ss");
					count = eles[j].doc_count;
					b = false;
					for(var a in arr){
						if(arr[a].key == e_key){
							arr[a][key] = count;
							b = true;
							break;
						}
					}
					if(!b){
						var tmp = [];
						tmp = {"key":e_key};
						tmp[key] = count;
						arr.push(tmp);
					}
				}
			}
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			
			var stackedBarCharts = new StackedBarCharts();
			stackedBarCharts.width = width;
			stackedBarCharts.height = height;
			if(content.color == null || $.trim(content.color) == "")
				stackedBarCharts.colors = hs_color;
			else
				stackedBarCharts.colors = [content.color];
			stackedBarCharts.num = n;
			stackedBarCharts.showX = content.xAxis;
			stackedBarCharts.showY = content.yAxis;
			stackedBarCharts.legend = content.legend;
			if(content.showTitle)
				stackedBarCharts.title = content.title;
			stackedBarCharts.xRotate = content.xRotate;
			stackedBarCharts.draw("#panel_content_"+n,arr);
		}
		$scope.generateCharts = function(datas,style,n,content){
			var ds = new Array();
			var key,count;
			if(content.aggsType == 'time'){
				for(var i=0;i<datas.length;i++){
					key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
					count = datas[i].doc_count;
					ds[i] = [$filter('date')(key,"yyyy-MM-dd HH:mm:ss"),count];
				}
			}else if(content.aggsType == 'field'){
				for(var i=0;i<datas.length;i++){
					key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
					count = datas[i].doc_count;
					ds[i] = [key,count];
				}
			}
			ds = $scope.sortData(ds,content.sortterm,content.sort);
			var panel_data = [];
			for(var i=0;i<ds.length;i++){
				panel_data.push({
					"key":ds[i][0],
					"count":ds[i][1]
				});
			}
			var width = $("#panel_content_"+n).width();
			var height = $("#panel_content_"+n).height();
			if(style == "pie"){
				var pieCharts = new PieCharts();
				pieCharts.width = width;
				pieCharts.height = height;
				pieCharts.colors = hs_color;
				pieCharts.num = n;
				if(content.showTitle)
					pieCharts.title = content.title;
				pieCharts.legend = content.legend;
				pieCharts.draw("#panel_content_"+n,panel_data);
			}else if(style == "bar"){
				var barCharts = new BarCharts();
				barCharts.width = width;
				barCharts.height = height;
				if(content.color == null || $.trim(content.color) == "")
					barCharts.colors = hs_color;
				else
					barCharts.colors = [content.color];
				barCharts.num = n;
				barCharts.showX = content.xAxis;
				barCharts.showY = content.yAxis;
				barCharts.legend = content.legend;
				if(content.showTitle)
					barCharts.title = content.title;
				barCharts.xRotate = content.xRotate;
				barCharts.draw("#panel_content_"+n,panel_data);
			}
		}
		$scope.clearFormProperties = function(){
			$scope.panel.form = {
				"type":{"key":null},
				"height":280
			};
		};
		$scope.clearColumnProperties = function(){
			$scope.panel.column ={"type":{"key":null}};
			$scope.panel.colpanel =null;
		};

		$scope.cancelPanel = function(fun){
			$scope.clearFormProperties();
			$scope.clearColumnProperties();
			$scope.hideLayers();
		}
		$scope.saveToDB = function(id,save_panel_id){
			var obj = $("#"+id);
			var chartId = obj.attr($scope.panel.properties_name+"id");
			var type = obj.attr($scope.panel.properties_name+"type");
			var content = eval("("+obj.attr($scope.panel.properties_name+"content")+")");
			
			if (type=="histogram" || type == "sparklines") {
				content.requestUrl = $scope.panel.datas[id].requestUrl+"/_search?ignore_unavailable=true";
				content.requestParam = $scope.panel.datas[id].requestParam;
			}else if(type == "hits"){
				content.requestUrl = $scope.panel.datas[id].requestUrl+"/_count";
				content.requestParam = $scope.panel.datas[id].requestParam;
			}else if(type == "agg_stats"){
				content.requestUrl = $scope.panel.datas[id].requestUrl+"/_search?ignore_unavailable=true";
				content.requestParam = $scope.panel.datas[id].requestParam;
				content.AggStatsResdata=$scope.panel.datas[id].AggStatsResdata;
			}else if(type == "stats"){
				content.requestUrl = $scope.panel.datas[id].requestUrl+"/_search?ignore_unavailable=true";
				content.requestParam = $scope.panel.datas[id].requestParam;
				content.StatsResdata=$scope.panel.datas[id].StatsResdata;
			}
			
			var dbPanel = {
				"id":chartId,
				"name":content.title,
				"type":type,
				"content":JSON.stringify(content)
			};
			$http.post("dbPanel/save.hs",dbPanel).success(function(data){
				$("#"+save_panel_id).remove();
//				alert(hs_info.success);
			})
			.error(function(e){
				alert(hs_info.errors);
			});
		};
		$scope.showCode = function(){
			$('.field_charts_point_'+n).tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:'bottom'
			});
		};
		$scope.showLayers = function(){
			$("#panel_layer").css("display","block");
			$("#panel_layer").html($("#panel_popup"));
			$("#panel_layer #panel_popup").css("display","block");
		}
		$scope.hideLayers = function(){
			$("#panel_layer").css("display","none");
		};
		
		/** 加载SparkShell**/
		$scope.loadSparkshellPanel = function(panelList,i){
			var panelId = panelList[i].id;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			if(!$rootScope.sparkPanelId){
				$rootScope.sparkPanelId = panelId;
			}
			var str = "";
			str += "<div id='"+$scope.panel.name+panelId+"' style='width:"+(content.width)+";' class='drag_panel' db_panel_id='"+panelId+"' db_panel_name='"+panelList[i].name+"'>";
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div ng-include=\"'app/template/panel/sparkshellshow.html'\">"
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			str += "</div>";
			$("#panel_content").append(str);
			
			var obj = $("#"+$scope.panel.name+panelId);
			obj.attr($scope.panel.properties_name+"id",panelList[i].id);
			obj.attr($scope.panel.properties_name+"type",panelList[i].type);
			obj.attr($scope.panel.properties_name+"content",panelList[i].content);
			
			$scope.panel.datas[$scope.panel.name+panelId] = {
					requestUrl:content.requestUrl,
				};
		};
		
		/** 加载Text**/
		$scope.loadTextPanel = function(panelList,i){
			var panelId = panelList[i].id;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			
			var str = "";
			str += "<div id='"+$scope.panel.name+panelId+"' style='width:"+(content.width)+";' class='drag_panel' db_panel_id='"+panelId+"' db_panel_name='"+panelList[i].name+"'>";
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='"+panel_id+"' class='db_panel_content hs_content' style='word-break:break-all;font-size:"+content.fontsize+"pt'>"
			str += "<p>"+content.comment+"</p>"
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			str += "</div>";
			$("#panel_content").append(str);
			
			var obj = $("#"+$scope.panel.name+panelId);
			obj.attr($scope.panel.properties_name+"id",panelList[i].id);
			obj.attr($scope.panel.properties_name+"type",panelList[i].type);
			obj.attr($scope.panel.properties_name+"content",panelList[i].content);
			
		};
		/** load时用于拼text的 **/
		$scope.textDiv=function(columncontent,panel){
			var str="";
			var id=panel.id;
			var n=id.substring(id.lastIndexOf("_")+1,id.length);
			var obj =$("<div id='"+$scope.panel.name+n+"' ng-show='"+!panel.hide+"'></div>");
			obj.attr("style","padding:5px");
			$("#"+columncontent).append(obj);
			
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panel.title+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+n+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+n+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='panel_content_"+n+"' class='db_panel_content hs_content' style='word-break:break-all;width:100%;height:"+panel.height+";overflow:auto;font-size:"+panel.fontsize+"pt'>"
			str += "<p>"+panel.comment+"</p>"
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			var content = {
				"title":panel.title,
				"mode":panel.mode,
				"fontsize":panel.fontsize,
				"comment":panel.comment
			};
			
			obj.attr($scope.panel.properties_name+"id",$scope.panel.name+n);
			obj.attr($scope.panel.properties_name+"type",panel.type);
			obj.attr($scope.panel.properties_name+"content",JSON.stringify(content));
			obj.attr($scope.panel.properties_name+"panel",JSON.stringify(panel));
			obj.html(str);
			
			
		};
		/** 仪表盘加载Column**/
		$scope.loadColumnPanel = function(panelList, i){
			var contentOld=panelList[i].content;
			var panelId = panelList[i].id;
			var ptype=panelList[i].type;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			var panels=content.panels;
			
			var obj =$("<div id='"+$scope.panel.name+panelId+"' ></div>");
			obj.attr("style","width:"+(content.width));
			$("#panel_content").append(obj);
			
			var str="";
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div id='"+panel_id+"' class='hs_content panel_content'  >";
			str += "<div id='panel_col_content_"+panelId+"' style='margin:10px;background-color:#F0F0F0;'>"
			str += "</div>";
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			obj.html(str);
			//根据panels中的数据展示
			for(var i=0; i<panels.length; i++) {
				var panel=panels[i];
				panel.columnId=$scope.panel.name+panelId;
				if (panel.type=='text') {
					$scope.textDiv("panel_col_content_"+panelId,panel);
					
				} else if(type=='agg_stats'){
					//$scope.showAggStats(id,type,"100%",panels[i].title,"panel_col_content_"+panelId,panels[i]);
				}
			}
			obj.attr($scope.panel.properties_name+"id",panelId);
			obj.attr($scope.panel.properties_name+"type",ptype);
			obj.attr($scope.panel.properties_name+"content",contentOld);
			
		}
		/** 仪表盘加载stats**/
		$scope.loadStatsPanel = function(panelList, i){
			var searchWay = "_search?pretty";
			var panelId = panelList[i].id;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			$scope.StatsResdata={
				"rows":content.StatsResdata.rows,
				"valueshow":content.StatsResdata.valueshow
			};
			$scope.panel.statsCheck1=content.statsCheck;
			
			var alt = "curl -XGET";
			alt += " '"+content.requestUrl+"/"+searchWay+"' -d<br/>";
			alt += " '" + JSON.stringify(content.requestParam)+"'";
			
			var str = "";
			str += "<div id='"+$scope.panel.name+panelId+"' style='width:"+(content.width)+";' class='drag_panel' db_panel_id='"+panelId+"' db_panel_name='"+panelList[i].name+"'>";
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div class='hs_content' id='"+panel_id+"' style='height:320px;width:100%;overflow:auto;'>";
			str += "<style>";
			str += "table.stats-table th, table.stats-table td {text-align: right;}"
			str += "table.stats-table th:first-child,  table.stats-table td:first-child {text-align: left;}"
			str += "</style>"
    		str += "<h1 style='text-align: center;height:100; line-height: 1.5em;padding-top:10px ; font-size:"+content.fontsize.value+"'><strong>{{StatsResdata.valueshow|formatstats:"+content.format+"}}</strong> <small style='font-size:.6em; line-height: 0;'>"+content.unit+"("+content.statsShow+")</small></h1>"
      		if (content.display_breakdown == 'yes') {
				str += "<table cellspacing='0' class='table-hover table table-condensed stats-table' style='margin-top: 38px;'>";
				str += "<tbody><thead><tr><th Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
				str += "<a style='font-size:15px;'><strong>" + content.label_name + "</strong></a></th>";
				str += "<th ng-repeat='stat in panel.statsCheck1' Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
				str += "<a href='' style='font-size:15px;'><strong>{{stat}}</strong></a></th></tr></thead>";
				str += "<tr ng-repeat='row in StatsResdata.rows' Bgcolor='#FFFFFF'>";
				str += "<td ><i class='fa fa-circle' ng-style='{color:row.color}'></i> {{row.name}}</td>";
				str += "<td ng-repeat='stat in panel.statsCheck1'>{{row.resvalue[stat]|formatstats:"+content.format+"}}" + content.unit + "</td>";
				str += "</tr>";
				str += "</tbody></table>";
			}
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			str += "</div>";
			$("#panel_content").append(str);
			
			$("#"+$scope.panel.name+panelId+" .panel_code").attr("title",alt);
			$("#"+$scope.panel.name+panelId+" .panel_code").attr("alt",alt);
			$("#"+$scope.panel.name+panelId+" .panel_code").tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:'bottom',
				multiple: true
			});
			
			var obj = $("#"+$scope.panel.name+panelId);
			obj.attr($scope.panel.properties_name+"id",panelList[i].id);
			obj.attr($scope.panel.properties_name+"type",panelList[i].type);
			obj.attr($scope.panel.properties_name+"content",panelList[i].content);
			
			$scope.panel.datas[$scope.panel.name+panelId] = {
				requestUrl:content.requestUrl,
				requestParam:content.requestParam
			};
		};
		/**仪表盘加载agg——stats**/
		$scope.loadAggStatsPanel = function(panelList, i){
			var searchWay = "_search?pretty";
			var panelId = panelList[i].id;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			$scope.AggStatsResdata={
				"rows":content.AggStatsResdata.rows,
				"valueshow":content.AggStatsResdata.valueshow
			};
			$scope.panel.statsCheck=content.statsCheck;
			
			var alt = "curl -XGET";
			alt += " '"+content.requestUrl+"/"+searchWay+"' -d<br/>";
			alt += " '" + JSON.stringify(content.requestParam)+"'";
			
			var str = "";
			str += "<div id='"+$scope.panel.name+panelId+"' style='width:"+(content.width)+";' class='drag_panel' db_panel_id='"+panelId+"' db_panel_name='"+panelList[i].name+"'>";
			str += "<div class='panel_title hs_title'>";
			str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
			str += "<div class='panel_title_icon'>";
			str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
			str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
			str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
			str += "</div>";
			str += "</div>";
			str += "<div class='db_panel_content hs_content' id='"+panel_id+"' style='height:320px;width:100%;overflow:auto;'>";
			str += "<style>";
			str += "table.stats-table th, table.stats-table td {text-align: right;}"
			str += "table.stats-table th:first-child,  table.stats-table td:first-child {text-align: left;}"
			str += "</style>"
    		str += "<h1 style='text-align: center;height:100; line-height: 1.5em;padding-top:10px ; font-size:"+content.fontsize.value+"'><strong>{{AggStatsResdata.valueshow|formatstats:"+content.format+"}}</strong> <small style='font-size:.6em; line-height: 0;'>"+content.unit+"("+content.statsShow+")</small></h1>"
      		if (content.display_breakdown == 'yes') {
				str += "<table cellspacing='0' class='table-hover table table-condensed stats-table' style='margin-top: 38px;'>";
				str += "<tbody><thead><tr><th Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
				str += "<a style='font-size:15px;'><strong>" + content.label_name + "</strong></a></th>";
				str += "<th ng-repeat='stat in panel.statsCheck' Bgcolor='#FFFFFF' style='border-bottom-color:#FFFFFF'>";
				str += "<a href='' style='font-size:15px;'><strong>{{stat}}</strong></a></th></tr></thead>";
				str += "<tr ng-repeat='row in AggStatsResdata.rows' Bgcolor='#FFFFFF'>";
				str += "<td ><i class='fa fa-circle' ng-style='{color:row.color}'></i> {{row.name}}</td>";
				str += "<td ng-repeat='stat in panel.statsCheck'>{{row.resvalue[stat]|formatstats:"+content.format+"}}" + content.unit + "</td>";
				str += "</tr>";
				str += "</tbody></table>";
			}
			str += "</div>";
			str += "<div class='hs_bottom'></div>";
			str += "</div>";
			$("#panel_content").append(str);
			
			$("#"+$scope.panel.name+panelId+" .panel_code").attr("title",alt);
			$("#"+$scope.panel.name+panelId+" .panel_code").attr("alt",alt);
			$("#"+$scope.panel.name+panelId+" .panel_code").tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:'bottom',
				multiple: true
			});
			
			var obj = $("#"+$scope.panel.name+panelId);
			obj.attr($scope.panel.properties_name+"id",panelList[i].id);
			obj.attr($scope.panel.properties_name+"type",panelList[i].type);
			obj.attr($scope.panel.properties_name+"content",panelList[i].content);
			
			$scope.panel.datas[$scope.panel.name+panelId] = {
				requestUrl:content.requestUrl,
				requestParam:content.requestParam
			};
		};
		
		$scope.loadPanelTerm = function(panelList,i,type,db_now,db_from,indices){
			var searchWay = "_search?pretty";
			if(type == "hits") searchWay = "_count";
			
			var panelId = panelList[i].id;
			var content = eval("("+panelList[i].content+")");
			var panel_id = "panel_content_"+panelId;
			var save_panel_id = $scope.panel.saveId+panelId;
			
			if(content.requestParam.query.range)
				content.requestParam["query"]["range"]["@timestamp"] = {"from":db_from};
			else if(content.requestParam.query.bool)
				if(content.requestParam.query.must)
					content.requestParam.query.must[0].range["@timestamp"] = {"from":db_from};

			var urls = content.requestUrl.substring(hs_es.host_name.length);
			var url = hs_es.host_name + "/" + indices + urls;
			
			var alt = "curl -XGET";
			alt += " '"+url+"' -d<br/>";
			alt += " '" + JSON.stringify(content.requestParam)+"'";

			var panelDiv = $("#"+$scope.panel.name+panelId);
			if(panelDiv.size() == 0){
				var str = "";
				str += "<div id='"+$scope.panel.name+panelId+"' style='width:"+(content.width)+";' class='drag_panel' db_panel_id='"+panelId+"' db_panel_name='"+panelList[i].name+"'>";
				str += "<div class='panel_title hs_title'>";
				str += "<div class='panel_title_txt'>"+panelList[i].name+"</div>";
				str += "<div class='panel_title_icon'>";
				str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
				str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+$scope.panel.name+panelId+"\")'></i>";
				str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='delPanel(\""+panelId+"\")'></i>";
				str += "</div>";
				str += "</div>";
				str += "<div class='db_panel_content hs_content' id='"+panel_id+"' style='height:"+content.height+"px;'></div>";
				str += "<div class='hs_bottom'></div>";
				str += "</div>";
				$("#panel_content").append(str);
			}

			if(type == "hits")
				PanelHits.generates(url,content.requestParam,panelId,content,alt,panelList[i]);
			else if(type == "sparklines"){
				PanelSparklines.generates(url,content.requestParam,panelId,content,db_from,db_now,alt,panelList[i]);
			}else if(type == "histogram"){
				content.requestUrl = url;
				if(content.style == "stacked_bar"){
					$scope.generateStackedBarPanel(panel_id,panelId,content,alt,panelList[i]);
				}else{
					$scope.generatePanel(panel_id,panelId,content,alt,panelList[i]);
				}
				
			}
			
			$scope.panel.datas[$scope.panel.name+panelId] = {
				requestUrl:content.requestUrl,
				requestParam:content.requestParam
			};
		};
		$scope.dealPanelAttr = function(alt,panel){
			$("#"+$scope.panel.name+panel.id+" .panel_code").attr("title",alt);
			$("#"+$scope.panel.name+panel.id+" .panel_code").attr("alt",alt);
			$("#"+$scope.panel.name+panel.id+" .panel_code").tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:'bottom',
				multiple: true
			});
			
			var obj = $("#"+$scope.panel.name+panel.id);
			obj.attr($scope.panel.properties_name+"id",panel.id);
			obj.attr($scope.panel.properties_name+"type",panel.type);
			obj.attr($scope.panel.properties_name+"content",panel.content);
		};
		$scope.loadPanel = function(){
			var db_now = new Date();
			var db_from = hsTimes.getBeforeDateBySpecify($rootScope.global.systemTimeRange.timeUnit,$rootScope.global.systemTimeRange.timeValue,db_now);
			var timeRefresh = $rootScope.global.systemTimeRange.timeRefresh;
			Aliases.getIndices($scope.expand_range(db_from,db_now,"day")).then(function(indices){
				$http.post("dbPanel/list.hs").success(function(data){
					var panelList = data.model.panelList;
					$scope.dealPanel(panelList,db_now,db_from,indices);
				});
			});
		};
		$scope.dealPanel = function(panelList,db_now,db_from,indices){
			for(var i=0;i<panelList.length;i++){
				var type=panelList[i].type;
				if (type=="histogram" || type == "hits" || type == "sparklines") {
					$scope.loadPanelTerm(panelList,i,type,db_now,db_from,indices);	
				}else if (type=="text") {
					$scope.loadTextPanel(panelList,i);	
				}else if (type=="sparkshell") {
					$scope.loadSparkshellPanel(panelList,i);	
				}else if(type=="agg_stats") {
					$scope.loadAggStatsPanel(panelList,i);	
				}else if(type=="stats") {
					$scope.loadStatsPanel(panelList,i);	
				}else if(type=="column") {
					$scope.loadColumnPanel(panelList,i);	
				} 
				
			}
			$compile($("#panel_content").contents())($scope);
			$scope.bindPanelListener();
			$("#panel_content .drag_panel").on("mousedown", function () {
			    $(this).addClass("hs_cursor_move");
			}).on("mouseup", function () {
			    $(this).removeClass("hs_cursor_move");
			});
		};
		$scope.generatePanel = function(panel_id,panelId,content,alt,panel){
			var url = content.requestUrl;
			var param = content.requestParam;
			var aggsType = content.aggsType;
			$http.post(url,param).success(function(d){
				var datas = d.aggregations.aggs.buckets;
				var ds = new Array();
				var key,count;
				if(aggsType == 'time'){
					for(var i=0;i<datas.length;i++){
						key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
						count = datas[i].doc_count;
						ds[i] = [$filter('date')(key,"yyyy-MM-dd HH:mm:ss"),count];
					}
				}else if(aggsType == 'field'){
					for(var i=0;i<datas.length;i++){
						key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
						count = datas[i].doc_count;
						ds[i] = [key,count];
					}
				}
				ds = $scope.sortData(ds,content.sortterm,content.sort);
				var panel_data = [];
				for(var i=0;i<ds.length;i++){
					panel_data.push({
						"key":ds[i][0],
						"count":ds[i][1]
					});
				}
				$scope.dealPanelAttr(alt,panel);
				var width = $("#"+panel_id).width();
				var height = $("#"+panel_id).height();
				if(content.style == "pie"){
					var pieCharts = new PieCharts();
					pieCharts.width = width;
					pieCharts.height = height;
					pieCharts.colors = hs_color;
					pieCharts.num = panelId;
					if(content.showTitle)
						pieCharts.title = content.title;
					pieCharts.legend = content.legend;
					pieCharts.draw("#"+panel_id,panel_data);
				}else if(content.style == "bar"){
					var barCharts = new BarCharts();
					barCharts.width = width;
					barCharts.height = height;
					if(content.color == null || $.trim(content.color) == "")
						barCharts.colors = hs_color;
					else
						barCharts.colors = [content.color];
					barCharts.num = panelId;
					barCharts.showX = content.xAxis;
					barCharts.showY = content.yAxis;
					if(content.showTitle)
						barCharts.title = content.title;
					barCharts.legend = content.legend;
					barCharts.xRotate = content.xRotate;
					barCharts.draw("#"+panel_id,panel_data);
				}
			});
		};
		$scope.generateStackedBarPanel = function(panel_id,panelId,content,alt,panel){
			var url = content.requestUrl;
			var param = content.requestParam;
			var aggsType = content.aggsType;
			$http.post(url,param).success(function(d){
				var datas = d.aggregations.aggs.buckets;
				var ds = new Array();
				var key,count;
				var arr = [];
				var eles;
				for(var i in datas){
					key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
					eles = datas[i].times.buckets;
					for(var j in eles){
						var e_key = !!eles[j].key_as_string ? eles[j].key_as_string :eles[j].key;
						if(content.aggsType == 'time')
							e_key = $filter('date')(e_key,"yyyy-MM-dd HH:mm:ss");
						count = eles[j].doc_count;
						b = false;
						for(var a in arr){
							if(arr[a].key == e_key){
								arr[a][key] = count;
								b = true;
								break;
							}
						}
						if(!b){
							var tmp = [];
							tmp = {"key":e_key};
							tmp[key] = count;
							arr.push(tmp);
						}
					}
				}
				$scope.dealPanelAttr(alt,panel);
				var width = $("#"+panel_id).width();
				var height = $("#"+panel_id).height();
				
				var stackedBarCharts = new StackedBarCharts();
				stackedBarCharts.width = width;
				stackedBarCharts.height = height;
				if(content.color == null || $.trim(content.color) == "")
					stackedBarCharts.colors = hs_color;
				else
					stackedBarCharts.colors = [content.color];
				stackedBarCharts.num = panelId;
				stackedBarCharts.showX = content.xAxis;
				stackedBarCharts.showY = content.yAxis;
				stackedBarCharts.legend = content.legend;
				if(content.showTitle)
					stackedBarCharts.title = content.title;
				stackedBarCharts.xRotate = content.xRotate;
				stackedBarCharts.draw("#"+panel_id,arr);
			});
		};
		
		$scope.bindPanelListener = function(){
			Element.prototype.hasClassName = function(name) {
			  	return new RegExp("(?:^|\\s+)" + name + "(?:\\s+|$)").test(this.className);
			};
			Element.prototype.addClassName = function(name) {
			  	if (!this.hasClassName(name)) {
			    	this.className = this.className ? [this.className, name].join(' ') : name;
			  	}
			};
			Element.prototype.removeClassName = function(name) {
			  	if (this.hasClassName(name)) {
			    	var c = this.className;
			    	this.className = c.replace(new RegExp("(?:^|\\s+)" + name + "(?:\\s+|$)", "g"), "");
			  	}
			};
			(function(){
				var cols = document.querySelectorAll('#panel_content .drag_panel');
				var dragSrcEl_ = null;
				this.panelDragStart = function(e){
					e.dataTransfer.effectAllowed = 'move';
				    e.dataTransfer.setData('text/html', this.innerHTML);

				    dragSrcEl_ = this;
				    this.addClassName('panel_moving');
				};
				this.panelDragEnter = function(e) {
				    this.addClassName('panel_over');
				};
				this.panelDragOver = function(e) {
				    if (e.preventDefault) {
				      	e.preventDefault();
				    }
				    e.dataTransfer.dropEffect = 'move';

				    return false;
				};
				this.panelDragLeave = function(e) {
				    this.removeClassName('panel_over');
				};
				this.panelDrop = function(e) {
				    if (e.stopPropagation) {
				      	e.stopPropagation();
				    }
				    if (dragSrcEl_ != this) {
				    	this.removeClassName('panel_moving');
				    	dragSrcEl_.removeClassName('panel_moving');
				    	$(dragSrcEl_).clone(true,true).insertAfter($(dragSrcEl_));
				    	$(this).clone(true,true).insertAfter($(this));
				    	$("div[pro_id="+this.attributes.pro_id.nodeValue+"]").eq(1).replaceWith($(dragSrcEl_));
				    	if($("div[pro_id="+dragSrcEl_.attributes.pro_id.nodeValue+"]").eq(0).is($(dragSrcEl_))){
				    		$("div[pro_id="+dragSrcEl_.attributes.pro_id.nodeValue+"]").eq(1).replaceWith($(this));
				    	}else{
				    		$("div[pro_id="+dragSrcEl_.attributes.pro_id.nodeValue+"]").eq(0).replaceWith($(this));
				    	}
				    }

				    return false;
				};
				this.panelDragEnd = function(e) {
			      	[].forEach.call(cols, function (col) {
				      	col.removeClassName('panel_over');
				      	col.removeClassName('panel_moving');
				    });
			      	$scope.savePosition();//保存位置到数据库中
			      	$compile($("#panel_content").contents())($scope);//重新编译
				};
				[].forEach.call(cols, function (col) {
					col.setAttribute('draggable', 'true');
				    col.addEventListener('dragstart', this.panelDragStart, true);
				    col.addEventListener('dragenter', this.panelDragEnter, true);
				    col.addEventListener('dragover', this.panelDragOver, true);
				    col.addEventListener('dragleave', this.panelDragLeave, true);
				    col.addEventListener('drop', this.panelDrop, true);
				    col.addEventListener('dragend', this.panelDragEnd, true);
				});
			})();
		};
		
		$scope.savePosition = function(){
			var n = 1;
			var arr = [];
			$("#panel_content .drag_panel").each(function(){
				arr.push({
					"id":$(this).attr("db_panel_id"),
					"position":(n++).toString()
				});
			});
			$http.post("dbPanel/updatePosition.hs",arr).success(function(){
//				alert(hs_info.success);
			}).error(function(){
				alert(hs_info.errors);
			});
		};
		
		$scope.delPanel = function(id){
			if(confirm("确定要删除该面板？")){
				var dbPanel = {"id":id};
				$http.post("dbPanel/del.hs",dbPanel).success(function(){
					$("#"+$scope.panel.name+id+"").remove();
				});
			}
		};
		
		$scope.removePanel = function(id){
			$("#charts_"+id).remove();
			if($scope.panel.timerId["time_id_"+id])
				$timeout.cancel($scope.panel.timerId["time_id_"+id]);
		}
		
		$scope.initShow = function(){
			$scope.loadPanel();
			var pTimer;
			var timeRefresh = $rootScope.global.systemTimeRange.timeRefresh;
			$scope.timeout = function(){
				pTimer = $timeout(function(){
					$scope.loadPanel();
					$scope.timeout();
				},timeRefresh*1000);
			};
			$scope.$on('$destroy', function() {
				$timeout.cancel(pTimer);
			});
			$scope.timeout();
			$scope.initPanelModule("show");
		};
		$scope.sortData = function(ds,sortterm,sort){
			var n = 1;
			if(sortterm == "term") n = 0;
			if(sort == "asc"){
				ds.sort(function(a,b){
					return a[n] > b[n];
				});
			}else{
				ds.sort(function(a,b){
					return a[n] < b[n];
				});
			}
			return ds;
		};
		/**初始化sparklines**/
		$scope.initSparklines = function(){
			$("#colorpicker").spectrum({showInput: true});
			if(!$scope.panel.form.aggs1){
				$scope.panel.form.aggs1 = [];
				for(var a in $scope.panel.aggs1.value){
					if($scope.panel.aggs1.value[a].key == "time"){
						$scope.panel.form.aggs1["type"] = $scope.panel.aggs1.value[a];
					}
				}
				
				if(!$scope.panel.form.aggs1.range){
					$scope.panel.form.aggs1.range = [];
				}
				if(!$scope.panel.form.aggs1.range.value){
					$scope.panel.form.aggs1.range.value = 5;
				}
				for(var p in $scope.panel.aggs.value[0].properties){
					if($scope.panel.aggs.value[0].properties[p].key == "minute"){
						$scope.panel.form.aggs1.range.type = $scope.panel.aggs.value[0].properties[p];
						$scope.panel.form.aggs1.range.unit = $scope.panel.aggs.value[0].properties[p];
					}
				}
			}
			if(!$scope.panel.form.refresh){
				$scope.panel.form.refresh = 1;
			}
		};
		/**初始化initHistogram**/
		$scope.initHistogram = function(){
			$("#colorpicker").spectrum({
				allowEmpty:true,
				showInput: true
			});
		};
		$scope.expand_range = function(starts, end, interval){
			var range;
	        var start = moment(starts).clone();
	        range = [];
	        while (start.isBefore(end)) {
	          range.push(start.clone());
	          switch (interval) {
	          case 'hour':
	            start.add(1,'hours');
	            break;
	          case 'day':
	        	start.add(1,'days');
	            break;
	          case 'week':
	        	start.add(1,'weeks');
	            break;
	          case 'month':
	        	start.add(1,'months');
	            break;
	          case 'year':
	        	start.add(1,'years');
	            break;
	          }
	        }
	        range.push(moment(end).clone());
	        return range;
		}
	}]);
	 app.filter('formatstats', function() {
        return function(value, format) {
            switch (format) {
                case 'money':
                    value = numeral(value).format('$0,0.00');
                    break;
                case 'bytes':
                    value = numeral(value).format('0.00b');
                    break;
                case 'float':
                    value = numeral(value).format('0.000');
                    break;
                default:
                    value = numeral(value).format('0,0');
            }
            return value;
        };
    });
});