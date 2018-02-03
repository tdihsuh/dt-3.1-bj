define(['app','scripts/factories/logs','d3','scripts/factories/pieCharts','scripts/factories/barCharts','scripts/factories/charts/sparklineCharts'],function(app,LogItems,d3,pieCharts,BarCharts,PieCharts,SparklineCharts){
	app.controller('searchCtrl',['$scope','$http','$filter','$sce','LogItems','hs_es','hs_color','hs_info','$routeParams','$rootScope','BarCharts','PieCharts','SparklineCharts',
	                             function($scope,$http,$filter,$sce,LogItems,hs_es,hs_color,hs_info,$routeParams,$rootScope,BarCharts,PieCharts,SparklineCharts){
		$rootScope.app_nav = "首页 / 搜索";
		$("#cmbbody").css("overflow","auto");
		$rootScope.search = {
			"fields":[]
		}
		if(!$scope.search){
			$scope.search = {};
		};
		$scope.search.list = [];
		$scope.search.list.title = [];
		$scope.search.data = [];//数据格式：［yyyy-MM-dd HH:mm:ss,count］
//		$scope.search.fields = [];//字段列表
		$scope.search.invalidFields = ["#","-"];//忽略的字段
		$scope.search.fieldPages = {
				"pageSize":40,
				"totalPage":0,
				"currentPage":1
		};//字段分页
		$scope.search.url = "";
		$scope.search.param = "";

		$scope.search.panel=[];
		$scope.search.panel.name = "charts_";
		$scope.search.panel.properties = "pro_";
		$scope.search.panel.saveId = "save_panel_";	//面板中保存按钮的id

		$scope.search.panel.id = "panel";

		/**默认取近一周数据**/
		initDatetime = function(){
			$scope.search.datetime_to = new Date();
			$scope.search.datetime_to = hsTimes.getBeforeDateTimeBySecond(0,$scope.search.datetime_to);
			$scope.search.datetime_from = hsTimes.getBeforeDateTimeByDay(1,$scope.search.datetime_to);
		}
		initDatetime();
		$scope.query = function(currentPage){
			if(!$rootScope.panel){
				$rootScope.panel = [];
			}
			$scope.search.logNum = 0;
			if(!$scope.search.datetime_to) $scope.search.datetime_to = new Date();
			var days = hsTimes.getDaysBetweenDate($scope.search.datetime_from,$scope.search.datetime_to);
			if(days.length > 0){
				var days_ = "";
				for(var i=0;i<days.length;i++){
					if(i > 0) days_ += ",";
					days_ += hs_es.log_index_name+days[i].format("yyyyMMdd");
				}
				$http.get(hs_es.host_name + "/" + days_  + "/_aliases?ignore_missing=true").success(function(data1){
					var n = 0;
					var valid_days = "";
					for(var o in data1){
						if(n > 0)
							valid_days += ",";
						valid_days += o;
						n ++;
					}
					$rootScope.panel.indices = valid_days;
					if(n > 0){
						$scope.search.url = hs_es.host_name + "/" + valid_days;
						$scope.queryFields();
						$scope.queryLogs(currentPage);
					}else{
						$scope.search.url = null;
						$rootScope.search.fields = [];
						generateSearchSummary();
						$scope.highlight = null;
						$scope.logs = null;
						$scope.logPages = null;
						$("#searchContainer").empty();
					}
				}).error(function(e){
					$("#searchContainer").empty();
					alert(hs_info.errors);
				});
			}
		};

		$scope.toggle = function(obj){
			if ($('#presetWrapper').is(":hidden")) {
				$('#presetWrapper').slideDown();
				$('#button_toggle').html('收起预设<span class="caret"></span>');
			} else {
				$('#presetWrapper').slideUp();
				$('#button_toggle').html('展开预设<span class="caret"></span>');
			}
			/**每次展开下拉菜单时清空之前的选择结果**/
			//initDatetime();
			if($scope.search.intervalId){
				clearInterval($scope.search.intervalId);
				$scope.search.intervalId = null;
			}
		}
		
		$scope.intervalSearch = function(txt,second){
			var time = second*1000;
			$scope.search.intervalId = setInterval($scope.query,time);
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			$('#presetWrapper').slideUp();
		}
		
		$scope.queryLogs = function(currentPage){
			waitter.show();
			$scope.search.keywords = $("#s_key").val();
			if($routeParams.param)
				if($scope.search.keywords)
					$scope.search.keywords += " "+$routeParams.param;
				else
					$scope.search.keywords = $routeParams.param;
			var param = {};
			param["sort"] = {
				'@timestamp':{
					order:"desc"
				}
			}
			var date_condiction = analyseDatetime();
			var keywords = analyseKeyWords();
			
			if(date_condiction && keywords){
				param["query"] = {};
				param["query"]["bool"] = {};
				param["query"]["bool"]["must"] = [];
				param["query"]["bool"]["must"][0] = {
					query_string:{
						query:keywords
					}
				};
				param["query"]["bool"]["must"][1] = date_condiction;
			}else if(keywords){
				param["query"] = {};
				param["query"] = {
					query_string:{
						query:keywords
					}
				};
			}else if(date_condiction){
				param["query"] = {};
				param["query"] = date_condiction;
			}
			$scope.search.param = param;
			if(!$rootScope.panel){
				$rootScope.panel = [];
			}
			$rootScope.panel.param = param;//更新面板(panel)参数
			LogItems.query($scope.search.url +"/_search?ignore_unavailable=true",param,currentPage).then(function(data){
				$scope.logs = data;
				var logPageNum = data.pageNum;
				var pages = [];
				var n = 0;
				var curr = data.currentPage;
				for(var i=curr-5;i<curr-1;i++){
					if(i >= 0)
						pages[n++] = i+1;
				}
				pages[n++] = curr;
				for(var i=curr+1;i<curr+5 && i <= logPageNum;i++){
					pages[n++] = i;
				}
				$scope.logPages = pages;
				
				$scope.search.logNum = $scope.logs.logNum;
				
				var took = $scope.logs.took;
				if(took > 1000){
					$scope.search.tookValue = $filter("number")(took/1000,2);
					$scope.search.tookUnit = "s";
				}else{
					$scope.search.tookValue = took;
					$scope.search.tookUnit = "ms";
				}
				
				generateSearchSummary();
				waitter.hidden();
			})
			$scope.highlight = $scope.search.keywords;
			generateReport();
		}
		generateSearchSummary = function(){
			$scope.search.summary = {};
			$scope.search.summary.datetime = "";
			if($scope.search.datetime_from && $scope.search.datetime_to){
				$scope.search.summary.datetime = $filter('date')($scope.search.datetime_from, "yyyy-MM-dd HH:mm:ss") + " 至 " +$filter('date')($scope.search.datetime_to, "yyyy-MM-dd HH:mm:ss");
			}else if($scope.search.datetime_from){
				$scope.search.summary.datetime = $filter('date')($scope.search.datetime_from, "yyyy-MM-dd HH:mm:ss") + " 截止至目前";
			}else if($scope.search.datetime_from){
				$scope.search.summary.datetime = "截止至" + $filter('date')($scope.search.datetime_to, "yyyy-MM-dd HH:mm:ss");
			}
		}
		/**分析关键字查询条件**/
		analyseKeyWords = function(){
			var keywords = $scope.search.keywords;
			if(keywords){
				keywords = keywords.replace(/\s+and\s+/," AND ");
				keywords = keywords.replace(/\s+or\s+/," OR ");
				keywords += " AND NOT _type:anomaly";
			}else{
				keywords = " NOT _type:anomaly"
			}
			return keywords;
		}
		/**分析时间查询条件**/
		analyseDatetime = function(){
			var datetime_from = $scope.search.datetime_from;
			var datetime_to = $scope.search.datetime_to;
			if(datetime_from){
				var param = {};
				if(datetime_to){
					param["range"] = {
						'@timestamp':{
							from:datetime_from,
							to:datetime_to
						}
					}
				}else{
					param["range"] = {
						'@timestamp':{
							gte:datetime_from
						}
					}
				}
				return param;
			}
		}
		
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

		$scope.hideLayers = function(){
			$("#search_popup_layers").css("display","none");
		}
		
		fieldReport = function(field,n){
			var param = angular.copy($scope.search.param);
			var _field = field+".raw";
			param["aggs"] = {
				field:{
					terms:{
						field:_field
					}
				}
			};
			param["from"] = 0;
			param["size"] = 0;
			delete param["sort"];
			$scope.search.field_report = [];
			$http.post($scope.search.url + "/_search?ignore_unavailable=true",param).then(function(data){
				var datas = data.data.aggregations.field.buckets;

				if(datas.length == 0){
					param["aggs"] = {
						field:{
							terms:{
								field:field
							}
						}
					};
					$http.post($scope.search.url + "/_search?ignore_unavailable=true",param).then(function(data){
						var datas = data.data.aggregations.field.buckets;
						generateFieldReport(field,n,datas,data.data.hits.total);
					});
				}else{
					generateFieldReport(field,n,datas,data.data.hits.total);
				}
			});
		}

		generateFieldReport = function(field,n,datas,total){
			var p = $("#field_area_"+n).position();
			var str = "<div id='field_area_"+n+"_detail' class='field_detail btn-group open'>";
			str += "<ul class='dropdown-menu'>";
			
			str += "<li class='dropdown-menu_li field_detail_title'>";
			str += 		"<span class='field_detail_title_s'>"+field+" 字段分析</span>";
			str += 		"<i class='fa fa-times field_detail_close' title='关闭' onclick='fieldHidden(\"field_area_"+n+"_detail\")'></i>";
			str += "</li>";
			str += "<li class='dropdown-menu_li'>";
			str += 		"<div class='btn-group'>";
			str += 		"<a class='btn btn-default' href='javascript:' onclick='fieldCharts(\"barcharts_"+n+"\")'>条形图</a>";
			str += 		"<a class='btn btn-default' href='javascript:' onclick='fieldCharts(\"piecharts_"+n+"\")'>分布图</a>";
			// str += 		"<a class='btn btn-default' href='javascript:'>走势图</a>";
			str += 		"</div>";
			str += "</li>";
			str += "<li class='dropdown-menu_li'>";
			var key,count,other=total;
			var fields_arr = [];

			/**条形图**/
			str += "<ul id='barcharts_"+n+"' class='barcharts field_charts'>";
			for(var i=0;i<datas.length;i++){
				key = !!datas[i].key_as_string ? datas[i].key_as_string : datas[i].key;
				count = datas[i].doc_count;
				str += "<li class='field_detail_comm_"+(i%2)+"'>";
				str += "<div class='field_detail_no'>"+(i+1)+".</div>"
				str += "<div class='field_detail_name hs_font_11'>"+key+"</div>";
				str += "<div class='field_detail_pro'><progress class='search_progress search_progress_"+(i+1)+"' value='"+count+"' max='"+total+"' title='"+$filter("number")(datas[i].doc_count/total*100,2)+"%'></progress></div>";
				str += "<div class='field_detail_count'>"+count+"</div>";
				str += "</li>";

				fields_arr.push({"key":key,"count":count});

				other -= count;
			}
			fields_arr.push({"key":"other","count":other});
			str += "</ul>";

			/**饼状图**/
			str += "<div id='piecharts_"+n+"' class='piecharts field_charts'></div>";
			/**走势图**/
			str += "</li>";
			
			str += "</ul>";
			str += "</div>";
			$("#field_area_"+n).append(str);
			
			var pieCharts = new PieCharts();
			pieCharts.colors = hs_color;
			pieCharts.num = n;
			pieCharts.draw("#piecharts_"+n,fields_arr);
			
			$("#piecharts_"+n).css("display","none");

			$('#barcharts_'+n+' .search_progress').tooltipster({
				theme: 'tooltipster-shadow',
				contentAsHTML: true,
				interactive: true,
				position:"right",
				functionReady:function(){
					$(this).mousemove(function(e){
						var offset = {"left":e.pageX,"top":e.pageY}
						$(".tooltipster-base").offset(offset);
					});
					$(this).mouseover(function(e){
						var offset = {"left":e.pageX,"top":e.pageY}
						$(".tooltipster-base").offset(offset);
					});
				}
			});
		}

		fieldCharts = function(id){
			$(".field_charts").css("display","none");
			$("#"+id).css("display","");
		}
		
		fieldHidden = function(id){
//			$("#"+id).hide();
			$("#"+id).remove();
		}
		
		generateReport = function(){
			$scope.search.data = [];
			if(!$scope.search.aggs){
				$scope.search.aggs = "hour";
			}
			if(!$scope.search_report)
				$scope.search_report = {};
			var param = angular.copy($scope.search.param);
			param["aggs"] = {
				datetime:{
					date_histogram:{
						field:"@timestamp",
						interval:$scope.search.aggs
					}
				}
			};
			
			delete param["from"];
			delete param["size"];
			delete param["sort"];
			var times = new Array();
			$http.post($scope.search.url + "/_search?ignore_unavailable=true",param).then(function(data){
				var datas = data.data.aggregations.datetime.buckets;
				for(var i=0;i<datas.length;i++){
					times[i] = [$filter('date')(datas[i].key_as_string,"yyyy-MM-dd HH:mm:ss"),datas[i].doc_count];
				}
				times.sort(function(a,b){
					return a[0] > b[0];
				});
				for(var i=0;i<times.length;i++){
					$scope.search.data.push({
						"time":times[i][0],
						"count":times[i][1]
					});
				}
			}).then(function(){
				showReport($scope.search.data);
			});
		}
		
		/**按小时统计**/
		countReportByHour = function(){
			return {
				aggs:{
					datetime:{
						date_histogram:{
							field:'@timestamp',
							interval:hour
						}
					}
				}
			}
		}
		
		/**分析索引，过滤日期、count**/
		analyseDayReport = function(data){
			var indices = new Array();
			var eles = data.data.split(/\s+/);
			var n = 0;
			var names;
			var currDateStr;
			var currDate;
			var b;
			for(var i=0;i<eles.length;i++){
				if(i%2 == 0 && eles[i] != ""){
					names = eles[i].split("-")[1];
					//currDateStr = names.substring(0,4)+"-"+names.substring(4,6)+"-"+names.substring(6,8);
					currDateStr = names;
					currDate = new Date(currDateStr);
					b = true;
					if(!!$scope.search.datetime_from)
						if($scope.search.datetime_from > currDate) 
							b = false;
					if(!!$scope.search.datetime_to)
						if($scope.search.datetime_to < currDate)
							b = false;
					if(b)
						indices[n++] = [currDateStr,parseInt(eles[i+1])];
				}
			}
			indices.sort(function(a,b){
				return a[0] > b[0];
			});
			return indices;
		}
		
		showReport = function(indices){
			$("#searchContainer").empty();
			var width = $("#searchContainer").width();
			var height = $("#searchContainer").height();
			
			var barCharts = new BarCharts();
			barCharts.width = width;
			barCharts.height = height;
			barCharts.colors = ["#009933"];
			barCharts.num = '01';
			barCharts.key = "time";
			barCharts.margin.top = 0;
			barCharts.margin.bottom = 0;
			barCharts.draw("#searchContainer",$scope.search.data);
		}
		
		/***选择查询时间**/
		$scope.selectDatetime = function(txt,num){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			if(num){
				$scope.search.datetime_from = hsTimes.getBeforeDateTimeByMinute(num);
			}else{
				$scope.search.datetime_from = null;
			}
			$scope.search.datetime_to = null;
			$('#presetWrapper').slideUp();
		}
		$scope.selectDay = function(txt,num){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			$scope.search.datetime_from = hsTimes.getBeforeDateTimeByDay(num);
			$scope.search.datetime_to = null;
			$('#presetWrapper').slideUp();
		}
		$scope.selectPreMonth = function(txt){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			var preMonth = hsTimes.getBeforeDateTimeByMonth(1);
			$scope.search.datetime_from = hsTimes.getFirstDayOnMonth(preMonth);
			$scope.search.datetime_to = hsTimes.getLastDayOnMonth(preMonth);
			$('#presetWrapper').slideUp();
		}
		$scope.selectPreYear = function(txt){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			var preYear = hsTimes.getBeforeDateTimeByMonth(12);
			$scope.search.datetime_from = hsTimes.getFirstDayOnYear(preYear);
			$scope.search.datetime_to = hsTimes.getLastDayOnYear(preYear);
			$('#presetWrapper').slideUp();
		}
		$scope.selectPreWeek = function(txt){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			$scope.search.datetime_from = hsTimes.getFistDayOnPreWeek();
			$scope.search.datetime_to = hsTimes.getLastDayOnPreWeek();
			$('#presetWrapper').slideUp();
		}
		$scope.selectRange = function(txt){
			$('#button_toggle').html(txt+'<span class="caret"></span>');
			var fromdate = $("#datepicker_from").val();
			var fromtime = $("#datepicker_time_from").val();
			var todate = $("#datepicker_to").val();
			var totime = $("#datepicker_time_to").val();
			
			if(fromdate){
				if(fromtime) $scope.search.datetime_from = new Date(fromdate + "T"+ fromtime);
				else {
					$scope.search.datetime_from = new Date(fromdate);
					$scope.search.datetime_from = hsTimes.getBeforeDateTimeByDay(0,$scope.search.datetime_from);
				}
			}
			if(todate){
				if(totime) $scope.search.datetime_to = new Date(todate + "T"+ totime);
				else {
					$scope.search.datetime_to = new Date(todate);
					$scope.search.datetime_to = hsTimes.getBeforeDateTimeByDay(0,$scope.search.datetime_to);
				}
			}
			$('#presetWrapper').slideUp();
		}
		$scope.showLogDetail = function(detail) {
			var keywords = $scope.highlight;
			if(detail && keywords){
				var kws = keywords.replace(/\"/g,"").split(/\s+/);
				var pattern = /^\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
				for(var j=0;j<kws.length;j++){
					kws[j] = kws[j].replace(/\"/g,"");
					if(pattern.test(kws[j])){
						$(".log_detail_re:icontains('"+kws[j]+"')").addClass("highlight");
						$(".log_detail_comm:icontains('"+kws[j]+"')").addClass("highlight");
					}else if((/^(ftp|http|https):\/\//.test(kws[j]) || !/[\+\;\(\)]/.test(kws[j]))
								&& /\W/.test(kws[j]) && /\D/.test(kws[j]) && !(/[\u4e00-\u9fa5]/.test(kws[j]))){
						var elements = kws[j].match(/\w+|\:|\/\/|\.|\/|-/g);
						$(".log_detail_re").parent().each(function(){
							//if($(this).text().contains(kws[j])){
							if($(this).text().indexOf(kws[j]) != -1){
								var n = 0;
								$(this).find("span").each(function(){
									for(var i=0;i<elements.length;i++){
										if(n >= elements.length) break;
										if($(this).text() == elements[i]){
											n ++;
											$(this).addClass("highlight");
											break;
										}
									}
								});
							}
						});
					}else{
						$(".log_detail_re:icontains('"+kws[j]+"')").addClass("highlight");
						$(".log_detail_comm:icontains('"+kws[j]+"')").addClass("highlight");
					}
				}
			}
			return detail;
//			return $sce.trustAsHtml(detail);
		};
		$(function(){
			$("#datepicker_from").datepicker({ changeMonth:true,changeYear: true , dateFormat:"yy-mm-dd"});
			$("#datepicker_to").datepicker({ changeMonth:true,changeYear: true , dateFormat:"yy-mm-dd"});
		});
		$scope.searchAggs = function(aggs){
			$scope.search.aggs = aggs;
			generateReport();
		}
		$scope.$on('ngRepeatFinished', function (ngRepeatFinishedEvent) {
			window.aaa = ngRepeatFinishedEvent.currentScope.search;
			$(".details_span").each(function(){
				var detail = $scope.showLogDetail($(this).html());
				detail = detail.replace(/&lt;/g,"<").replace(/&gt;/g,">");
				$(this).html(detail);
				$(this).css("display","block");
			});
			$(".log_detail_re").bind({'mouseover':function(){
					$(this).css("backgroundColor","#FFCC99");
					$(this).prevAll().css("backgroundColor","#FFCC99");
				},'mouseout':function(){
					$(this).css("backgroundColor","");
					$(this).prevAll().css("backgroundColor","");
				},'dblclick':function(){
					var txt = "";
					$(this).prevAll().each(function(){
						txt = $(this).text() + txt;
					});
					txt += $(this).text();
					var s_key = $("#s_key").val();
					if(s_key){
						$("#s_key").val(s_key +" \"" + txt + "\"");
					}else{
						$("#s_key").val("\"" + txt + "\"");
					}
				}
			});
			$(".log_detail_comm").bind({'mouseover':function(){
					$(this).css("backgroundColor","#FFCC99");
				},'mouseout':function(){
					$(this).css("backgroundColor","");
				},'dblclick':function(){
					var s_key = $("#s_key").val();
					if(s_key){
						$("#s_key").val(s_key +" \"" + $(this).text() + "\"");
					}else{
						$("#s_key").val("\"" + $(this).text() + "\"");
					}
				}
			});
		});
		/**查询所有字段**/
		$scope.queryFields = function(){
			$http.get($scope.search.url + "/_mapping").success(function(data){
				var mapps;
				var props;
				for(var index in data){
					mapps = data[index].mappings;
					for(var i in mapps){
						props = mapps[i].properties;
						for(var j in props){
							if($rootScope.search.fields.indexOf(j) == -1 && $scope.search.invalidFields.indexOf(j) == -1){
								$rootScope.search.fields.push(j);
							}
						}
					}
				}
				if(!$rootScope.panel.virtualNum){
					$rootScope.panel.virtualNum = $rootScope.search.fields.length;
				}
				$scope.search.fieldPages.totalPage = parseInt(($rootScope.search.fields.length-1)/$scope.search.fieldPages.pageSize+1);
			});
		}

		$scope.modifyFieldTitle = function(field){
			var n = $scope.search.list.title.indexOf(field);
			if(n == -1){
				if($scope.search.list.title.length >=8){
					alert("选择的字段不能大于8个！");
					return;
				}
				$scope.search.list.title.push(field);
			}else{
				$scope.search.list.title.splice(n,1);
			}
		}
		$scope.showFieldDetail = function(field,n){
			$(".field_detail").remove();
//			$(".field_charts").css("display","none");
			$("#barcharts_"+n).css("display","");
			var obj = $("#field_area_"+n+"_detail");
			fieldReport(field,n);
		}
		$scope.query();
		$scope.showDetailFields = function(id){
			var b = false;
			if($("#log_"+id).css("display") == "none")
				b = false;
			else
				b = true;
			$(".detail_field").css("display","none");
			if(b)
				$("#log_"+id).css("display","none");
			else
				$("#log_"+id).css("display","block");
		}

		$(function(){
			var childrens = $("#search").children();
			for(var i=0;i<childrens.length;i++){
				if(childrens[i].id != "search_group"){
					$(childrens[i]).bind("click",function(){
						if($('#presetWrapper').is(":hidden")){
						}else{
							$('#presetWrapper').slideUp();
						}
					});
				}
			}
		});
	}])
});