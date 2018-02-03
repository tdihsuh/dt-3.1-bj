define(['app','d3'],function(app,d3){
	app.factory('BarCharts',function(){
		return function(){
			var barCharts = {
				key : "key",
				val : "count",
				width : 400,
				height : 280,
				num : 0,//标识为第几个chart
				colors : ["#92A2A8"],
				margin : {top:5, right:5, bottom: 5, left: 5},
				showX : false,
				showY : false,
				title : "",
				legend : "",
				xRotate:null,
				draw:function(element,data){
					
					var key = this.key;
					var val = this.val;
					var width = this.width;
					var height = this.height;
					var num = this.num;
					var colors = this.colors;
					var margin = this.margin;
					var legend = this.legend;
					
					d3.select("#bar_"+num).remove();
					d3.select(element).append("div").attr("id","bar_"+num).attr("class","hs_bar");
					var parentId = "#bar_"+num;
					
					/**判断是否显示标题**/
					if(this.title != null && $.trim(this.title) != "") {
						var p_title = d3.select(parentId).append("div").attr("class","hs_charts_title").text(this.title);
						height = height-p_title.style("height").substring(0,p_title.style("height").indexOf("px"))-5;
					}
					
					/**如果显示Y轴，则计算y轴宽度**/
					if(this.showY){
						var n = 0;
						var len = 0;
						for(var i in data){
							len = data[i][val].toString().length;
							n = n < len ? len:n;
						}
						margin.left += n*8+10;
					}
					/**如果显示X轴，则计算x轴高度**/
					var xHeight = 0;
					if(this.showX){
						var n = 0;
						var len = 0;
						for(var i in data){
							len = data[i][key].toString().length;
							n = n < len ? len:n;
						}
						xHeight = n*6;
						xHeight = xHeight < 25 ? 25 : xHeight;
					}
					
					var color = d3.scale.ordinal().range(colors);
					/**如果显示图例**/
					if(legend != null && legend != "" && legend != "none"){
						var desc = d3.select(parentId).append("div")
							.attr("class","panel_bar_desc")
							.attr("id","bar_desc_"+num);
						
						var descs = d3.select("#bar_desc_"+num).append("div:ul").attr("class","panel_bar_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_bar_desc_"+num).data(data).enter().append("ul.panel_bar_desc_"+num+":li")
							.attr("class","icon-circle hs_left hs_charts_desc_ul_li")
							.style("color",function(d,i){return color(i);})
							.text(function(d,i){return d[key] + " (" + d[val] + ") ";});
						
						height = height-descs.style("height").substring(0,descs.style("height").indexOf("px"))-5;
					}
					if(legend == "below")
						desc.remove();
					
					/**计算宽度**/
					var canvasWidth = width - margin.left - margin.right;
					var canvasHeight = height - margin.top - margin.bottom;
					/**如果显示X轴，重新计算画布高度**/
					if(this.showX){
						canvasHeight -= (xHeight/2);
					}
					
					var svg = d3.select(parentId)
						.append("svg:svg")
						.attr("width", "100%")
						.attr("height", height)
						.attr("id", "bar_charts_"+num)
						;
					
					x = d3.scale.ordinal().domain(d3.range(data.length)).rangeRoundBands([0,canvasWidth],.1);
					y = d3.scale.linear().domain([0, d3.max(data, function(d){return d[val];})]).range([ canvasHeight,0]);
					 
					var bar = d3.select("#bar_charts_"+num)
						.append("svg:g")
						.attr("class", "panel_bar_"+num)
						.attr("transform", "translate("+margin.left+","+margin.top+")");
					 
					var bars = bar.selectAll("g.panel_bar_"+num)
						.data(data)
						.enter().append("svg:g")
						.attr("class", "bar")
						;
					
					var paths = bars.append("svg:rect")
						.attr("fill", function(d, i) { return color(i); })
//						.attr("fill-opacity", .6)
						.attr("x", function(d,i) { return x(i) })
						.attr("y", function(d) { return y(d[val])})
						.attr("width", function(d) { return x.rangeBand() })
						.attr("height", function(d) {return canvasHeight-y(d[val]) })
						.attr("class","bar_charts_point_"+num+" hs_bar_alt")
						.on("mouseover",function(d,i){
							d3.select("#bar_popup_"+num)
						        .html(function(){
						        	return d[key] + "<br/>" + d[val];
						        })
						        .style("left", (d3.event.pageX-$("#bar_"+num).offset().left) + "px")
						        .style("top", (d3.event.pageY-$("#bar_"+num).offset().top-$("#bar_"+num).height()-margin.top-margin.bottom) + "px")
						        .attr("class","hs_bar_popup")
						        ;
						})
						.on("mousemove",function(d,i){
							d3.select("#bar_popup_"+num)
						        .html(function(){
						        	return d[key] + "<br/>" + d[val];
						        })
						        .style("left", (d3.event.pageX-$("#bar_"+num).offset().left) + "px")
						        .style("top", (d3.event.pageY-$("#bar_"+num).offset().top-$("#bar_"+num).height()-margin.top-margin.bottom) + "px")
						        .attr("class","hs_bar_popup")
						        ;
						})
						.on("mouseout",function(d,i){
							d3.select("#bar_popup_"+num)
								.on("mouseover",function(d,i){
									$(this).attr("class","hs_bar_popup")
								})
								.on("mouseout",function(d,i){
									$(this).attr("class","hs_hide")
								})
								.attr("class","hs_hide")
								;
						})
						;
					$(function(){
					});
					
					
					/**x,y轴**/
					if(this.showX){
						var xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat(function(d){return data[d][key]});
						var xg = svg.append("g")
				        .attr("class", "hs_charts_x_axis")
				        .attr("transform", "translate("+margin.left+"," + (canvasHeight+margin.top) + ")")
				        .call(xAxis)
				        ;
						if(!!this.xRotate){
							xg.selectAll("text")
							.attr("transform","translate(0,"+(xHeight/(90/this.xRotate)/2)+"),rotate(-"+this.xRotate+")")
							;
						}
					}
					if(this.showY){
						var yAxis = d3.svg.axis().scale(y).orient("left").ticks(5);
					    svg.append("g")
					        .attr("class", "hs_charts_y_axis")
					        .attr("transform", "translate("+margin.left+","+margin.top+")")
					        .call(yAxis);
					}
					
					if(legend == "below"){
						var desc = d3.select(parentId).append("div")
							.attr("class","panel_bar_desc")
							.attr("id","bar_desc_"+num);
						
						var descs = d3.select("#bar_desc_"+num).append("div:ul").attr("class","panel_bar_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_bar_desc_"+num).data(data).enter().append("ul.panel_bar_desc_"+num+":li")
							.attr("class","icon-circle hs_left hs_charts_desc_ul_li")
							.style("color",function(d,i){return color(i);})
							.text(function(d,i){return d[key] + " (" + d[val] + ") ";});
					}
					
					d3.select(parentId).append("div").attr("id","bar_popup_"+num).attr("class","hs_hide");
				}
			}
			return barCharts;
		};
		
	});
});