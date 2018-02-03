define(['d3'],function(d3){
	var module = function(element,data){
		if(data.length == 0) return;
		var margin = {top: 20, right: 80, bottom: 140, left: 50},
		width = 960 - margin.left - margin.right,
		height = 400 - margin.top - margin.bottom;
		
		//var parseDate = d3.time.format("%Y-%m-%d %H:%M:%S").parse;
 
		/**
		var minDate = parseDate(data[0].date);
		var maxDate = parseDate(data[data.length-1].date);
		
		var x = d3.time.scale()
			.domain([minDate,maxDate])
			.range([0, width]);**/
		var	x = d3.scale.ordinal()
			.domain(d3.range(data.length))
			.rangeRoundBands([0,width]);

		var y = d3.scale.linear()
			.range([height, 0]);

		var color = d3.scale.category10();

		var xAxis = d3.svg.axis()
			.scale(x)
			//.orient("bottom").ticks(10)
			.tickFormat(function(d){return data[d].sid})
			;

		var yAxis = d3.svg.axis()
			.scale(y)
			.orient("left");
			
		if(d3.select("svg")){
			d3.select("svg").remove();
		};
		var svg = d3.select(element).append("svg")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
		  .append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
			
		  color.domain(d3.keys(data[0]).filter(function(key) { return key !== "sid"; }));
		  /**
		  data.forEach(function(d) {
			d.date = parseDate(d.date);
		  });
		  **/
		  var cities = color.domain().map(function(name) {
			return {
			  name: name,
			  values: data.map(function(d) {
				return {sid: d.sid, temperature: +d[name]};
			  })
			};
		  });

		  //x.domain(d3.extent(data, function(d) { return d.sid; }));

		  y.domain([
			0,
			d3.max(cities, function(c) { return d3.max(c.values, function(v) { return v.temperature; }); })
		  ]);

		  var ag = svg.append("g")
			  .attr("class", "x axis")
			  .attr("transform", "translate(0," + height + ")")
			  .call(xAxis);
		ag.selectAll("text")
			.attr("transform","rotate(-90)")
			.attr("dy","-0.5em")
			.attr("x","-4em");
			;
		ag.append("text")
			.text("Session ID")
			.attr("transform","translate("+width+",0)")
			.attr("x",6);

		  svg.append("g")
			  .attr("class", "y axis")
			  .call(yAxis)
			.append("text")
			  .attr("transform", "rotate(-90)")
			  .attr("y", 6)
			  .attr("dy", ".71em")
			  .style("text-anchor", "end")
			  .text("url数量");
			  
		  var line = d3.svg.line()
			.interpolate("linear")
			.x(function(d,i) { return x.rangeBand()*(i+0.5); })
			.y(function(d) { return y(d.temperature); });

		  var city = svg.selectAll(".city")
			  .data(cities)
			.enter().append("g")
			  .attr("class", "city");

		  city.append("path")
			  .attr("class", "line")
			  .attr("d", function(d) { return line(d.values); })
			  .style("stroke", function(d) { return color(d.name); });

		  city.append("text")
			  .datum(function(d) { return {name: d.name, value: d.values[d.values.length - 1]}; })
			  .attr("transform", function(d,i) { return "translate(" + (width-x.rangeBand()/2+2) + "," + y(d.value.temperature) + ")"; })
			  .attr("x", 3)
			  .attr("dy", ".35em")
			  .attr("fill",function(d,i){return color(cities[i].name);})
			  .text(function(d) { return d.name; });
			  
			for(var i=0;i<cities.length;i++){
				var pointer = svg.selectAll(".point")
					.data(cities[i].values)
					.enter().append("svg:circle");
				pointer.attr("class","chart_point")
					.attr("stroke", color(cities[i].name))
					.attr("fill", color(cities[i].name))
					.attr("cx", function(d, i) { return x.rangeBand()*(i+0.5); })
					.attr("cy", function(d, i) { return y(d.temperature); })
					.attr("r", function(d, i) { return 3; })
					//.attr("transform",function(d){return "translate(-"+x.rangeBand()/2+",0)";})
					.attr("title",function(d){return "Session ID："+d.sid+"<br/>数量："+d.temperature;});
				pointer.on("mouseover",function(){
					return d3.select(this).attr("r",4);
				}).on("mouseout",function(){
					return d3.select(this).attr("r",3);
				});
			}
				
			$(function(){
				$('.chart_point').tooltipster({
					theme: 'tooltipster-shadow',
					contentAsHTML: true,
					interactive: true
				});
			});
	};
	return module;
});