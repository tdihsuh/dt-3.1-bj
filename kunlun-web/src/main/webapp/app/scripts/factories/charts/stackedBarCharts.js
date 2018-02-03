define(['app','d3'],function(app,d3){
	app.factory('StackedBarCharts',function(){
		return function(){
			var stackedBarCharts = {
				key : "key",
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
					/***
					 * 统一字段
					 */
					var fields = new Array();
					for(var i in data){
						for(var j in data[i]){
							if($.inArray(j,fields) == -1){
								fields.push(j);
							}
						}
					}
					for(var f in fields){
						for(var d in data){
							var b = false;
							for(var i in data[d]){
								if(fields[f] == i){
									b = true;
								}
							}
							if(!b){
								data[d][fields[f]] = 0;
							}
						}
					}
					
					var key = this.key;
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
							for(var j in data[i]){
								if(j == key) continue;
								len = data[i][j].toString().length;
								n = n < len ? len:n;
							}
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
					color.domain(d3.keys(data[0]).filter(function(k) { return k !== key; }));
					
					/**如果显示图例**/
					if(legend != null && legend != "" && legend != "none"){
						var desc = d3.select(parentId).append("div")
							.attr("class","panel_bar_desc")
							.attr("id","bar_desc_"+num);
						
						var descs = d3.select("#bar_desc_"+num).append("div:ul").attr("class","panel_bar_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_bar_desc_"+num).data(color.domain().slice().reverse()).enter().append("ul.panel_bar_desc_"+num+":li")
							.attr("class","icon-circle hs_left hs_charts_desc_ul_li")
							.style("color",function(d,i){return color(d);})
							.text(function(d,i){return d;});
						
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
					
					var x = d3.scale.ordinal().rangeRoundBands([0, canvasWidth], .1);
					var y = d3.scale.linear().rangeRound([canvasHeight, 0]);
					
					data.forEach(function(d) {
						var y0 = 0;
						d.groups = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
						d.total = d.groups[d.groups.length - 1].y1;
					});

					x.domain(data.map(function(d) { return d[key]; }));
					y.domain([0, d3.max(data, function(d) { return d.total; })]);
				  
				  var svg = d3.select(parentId).append("svg")
				    .attr("width", "100%")
				    .attr("height", height)
				    .append("g")
				    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

				  var bars = svg.selectAll("g.panel_bar_"+num)
						.data(data)
						.enter().append("svg:g")
						.attr("class", "stacked_bar")
						.attr("transform", function(d) { return "translate(" + x(d[key]) + ",0)"; });

				  bars.selectAll("rect")
				      .data(function(d) { return d.groups; })
				      .enter().append("rect")
				      .attr("width", x.rangeBand())
				      .attr("y", function(d) { return y(d.y1); })
				      .attr("height", function(d) { return y(d.y0) - y(d.y1); })
				      .style("fill", function(d) { return color(d.name); })
				      .style("stroke","#FFFFFF")
				      .style("stroke-width","0.5")
				      .attr("class","bar_charts_point_"+num+" hs_bar_alt")
				      .on("mouseover",function(d,i){
							d3.select("#bar_popup_"+num)
						        .html(function(){
						        	return d.name + "<br/>" + (d.y1-d.y0);
						        })
						        .style("left", (d3.event.pageX-$("#bar_"+num).offset().left) + "px")
						        .style("top", (d3.event.pageY-$("#bar_"+num).offset().top-$("#bar_"+num).height()-margin.top-margin.bottom) + "px")
						        .attr("class","hs_bar_popup")
						        ;
						})
						.on("mousemove",function(d,i){
							d3.select("#bar_popup_"+num)
						        .html(function(){
						        	return d.name + "<br/>" + (d.y1-d.y0);
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
					
					/**x,y轴**/
					if(this.showX){
						var xAxis = d3.svg.axis().scale(x).orient("bottom").tickFormat(function(d){return d;});
						var xg = svg.append("g")
				        .attr("class", "hs_charts_x_axis")
				        .attr("transform", "translate(0," + (canvasHeight) + ")")
				        .call(xAxis);
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
					        .call(yAxis);
					}
					
					if(legend == "below"){
						var desc = d3.select(parentId).append("div")
						.attr("class","panel_bar_desc")
						.attr("id","bar_desc_"+num);
					
						var descs = d3.select("#bar_desc_"+num).append("div:ul").attr("class","panel_bar_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_bar_desc_"+num).data(color.domain().slice().reverse()).enter().append("ul.panel_bar_desc_"+num+":li")
							.attr("class","icon-circle hs_left hs_charts_desc_ul_li")
							.style("color",function(d,i){return color(d);})
							.text(function(d,i){return d;});
						
						height = height-descs.style("height").substring(0,descs.style("height").indexOf("px"))-5;
					}
					
					d3.select(parentId).append("div").attr("id","bar_popup_"+num).attr("class","hs_hide");
				}
			}
			return stackedBarCharts;
		};
		
	});
});