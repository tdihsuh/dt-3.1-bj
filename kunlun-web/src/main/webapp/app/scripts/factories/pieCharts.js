define(['app','d3'],function(app,d3){
	app.factory('PieCharts',function(){
		return function(){
			var pieCharts = {
				key : "key",
				val : "count",
				width : 400,
				height : 280,
				num : 0,//标识为第几个chart
				colors : ["#92A2A8"],
				margin : {top:5, right:5, bottom: 5, left: 5},
				title : "",
				legend : "",
				draw:function(element,data){
					
					var key = this.key;
					var val = this.val;
					var width = this.width;
					var height = this.height;
					var num = this.num;
					var colors = this.colors;
					var margin = this.margin;
					var legend = this.legend;
					
					d3.select("#pie_"+num).remove();
					d3.select(element).append("div").attr("id","pie_"+num).attr("class","hs_pie");
					var parentId = "#pie_"+num;
					
					/**判断是否显示标题**/
					if(this.title != null && $.trim(this.title) != ""){
						var p_title = d3.select(parentId).append("div").attr("class","hs_charts_title").text(this.title);
						height = height-p_title.style("height").substring(0,p_title.style("height").indexOf("px"))-5;
					}
					
					var color = d3.scale.ordinal().range(colors);
					
					/**如果显示图例**/
					if(legend != null && legend != "" && legend != "none"){
						var desc = d3.select(parentId).append("div")
							.attr("class","panel_pie_desc")
							.attr("id","pie_desc_"+num);
						
						var descs = d3.select("#pie_desc_"+num).append("div:ul").attr("class","panel_pie_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_pie_desc_"+num).data(data).enter().append("ul.panel_pie_desc_"+num+":li")
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
					/**计算pie半径**/
					var radius = Math.min(canvasWidth, canvasHeight) / 2;

					var arc = d3.svg.arc()
					    .outerRadius(radius - 10)
					    .innerRadius(0);

					var pie = d3.layout.pie()
					    .sort(null)
					    .value(function(d) { return d[val]; });

					var svg = d3.select(parentId).append("svg")
					    .attr("width", canvasWidth)
					    .attr("height", canvasHeight)
					  	.append("g")
					    .attr("transform", "translate(" + canvasWidth / 2 + "," + canvasHeight / 2 + ")");

					data.forEach(function(d) {
						d[val] = +d[val];
					});

					var total= d3.sum(data, function(d){return d[val];});

					var g = svg.selectAll(".arc")
						.data(pie(data))
						.enter().append("g")
						.attr("class", "arc");

					g.append("path")
						.attr("d", arc)
						.attr("class","field_charts_point_"+num+" hs_pie_alt")
						.style("fill", function(d,i) { return color(i); })
						.style("stroke","#FFFFFF")
						.on("mouseover",function(d,i){
							d3.select("#pie_popup_"+num)
						        .html(function(){
						        	return d.data[key] + "<br/>" + d3.round(d.data[val]/total*100,1) + "%";
						        })
						        .style("left", (d3.event.pageX-$("#pie_"+num).offset().left) + "px")
						        .style("top", (d3.event.pageY-$("#pie_"+num).offset().top-$("#pie_"+num).height()-margin.top-margin.bottom) + "px")
						        .attr("class","hs_pie_popup")
						        ;
						})
						.on("mousemove",function(d,i){
							d3.select("#pie_popup_"+num)
						        .html(function(){
						        	return d.data[key] + "<br/>" + d3.round(d.data[val]/total*100,1) + "%";
						        })
						        .style("left", (d3.event.pageX-$("#pie_"+num).offset().left) + "px")
						        .style("top", (d3.event.pageY-$("#pie_"+num).offset().top-$("#pie_"+num).height()-margin.top-margin.bottom) + "px")
						        .attr("class","hs_pie_popup")
						        ;
						})
						.on("mouseout",function(d,i){
							d3.select("#pie_popup_"+num)
								.on("mouseover",function(d,i){
									$(this).attr("class","hs_pie_popup")
								})
								.on("mouseout",function(d,i){
									$(this).attr("class","hs_hide")
								})
								.attr("class","hs_hide")
								;
						})
						;

					g.append("text")
						.attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
						.attr("dy", ".35em")
						.style("text-anchor", "middle")
						.text(function(d) {
							var per = d.data[val]/total*100;
							if(per >= 5){
								return d3.round(per,1)+"%";
							}  
						});

					$(function(){
//						$('.field_charts_point_'+num).tooltipster({
//							theme: 'tooltipster-shadow',
//							contentAsHTML: true,
//							interactive: true,
//							position:"right",
//							functionReady:function(){
//								$(this).mousemove(function(e){
//									var offset = {"left":e.pageX,"top":e.pageY}
//									$(".tooltipster-base").offset(offset);
//								});
//								$(this).mouseover(function(e){
//									console.log(e.pageX);
//									var offset = {"left":e.pageX,"top":e.pageY}
//									$(".tooltipster-base").offset(offset);
//								});
//							}
//						});
					});
					
					if(legend == "below"){
						var desc = d3.select(parentId).append("div")
							.attr("class","panel_pie_desc")
							.attr("id","pie_desc_"+num);
						
						var descs = d3.select("#pie_desc_"+num).append("div:ul").attr("class","panel_pie_desc_"+num+" hs_charts_desc_ul");
						descs.selectAll("ul.panel_pie_desc_"+num).data(data).enter().append("ul.panel_pie_desc_"+num+":li")
							.attr("class","icon-circle hs_left hs_charts_desc_ul_li")
							.style("color",function(d,i){return color(i);})
							.text(function(d,i){return d[key] + " (" + d[val] + ") ";});
					}
					
					d3.select(parentId).append("div").attr("id","pie_popup_"+num).attr("class","hs_hide");
				}
			}
			return pieCharts;
		}
	});
});