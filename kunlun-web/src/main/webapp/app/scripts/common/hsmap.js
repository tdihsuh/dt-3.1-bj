function HsMap(){
	this.label=null,
	this.mapurl=null,
	this.map = null,
	/**
	 *destinationData
	 *攻击目标
	 *格式：
	 *"city":"北京",
	 *"longitude":116.40752599999996,
	 *"latitude":39.90403,
	 *"publicIp":116.211.213.20,
	 *"privateIps":[],
	 *"servers":{
	 *	"PBSZ-C":532819,
	 *	"PBSZ-A":521829,
	 *	"PBSZ-B":320126
	 *}
	 */
	this.destinationData = [];
	/**
	 *sourceData
	 *攻击源
	 *格式：
	 *  "longitude":121.55983449999997, 
	 *	"latitude":25.091075,
	 *	"city":"台北",
	 *  "attacksNum":582,
	 *	"ips":{
	 *		"123.125.232.184":91819,
	 *		"123.125.232.185":72881,
	 *		"123.125.232.183":16273
	 *	}
	 */
	this.sourceData = [];
	/**
	 *connectData
	 *攻击源与攻击目标关联关系
	 *	"city":"北京",
	 *	"longitude":116.40752599999996,
	 *	"latitude":39.90403,
	 *	"sources":[
	 *		{
	 *			"longitude":121.55983449999997, 
	 *			"latitude":25.091075,
	 *			"city":"台北",
	 *			"attacksNum":11021
	 *		}
	 *	]
	 */
	this.connectData = [];
	this.init = function(){
		var options = {
			projection : "EPSG:900913",
			displayProjection : new OpenLayers.Projection("EPSG:4326")
		};
		this.map = new OpenLayers.Map("map",options);
		var tms = new OpenLayers.Layer.OSM(
			"HS MAPS",
			this.mapurl+"/${z}/${x}/${y}.png",
			{numZoomLevels : 9}
		);
		this.map.addLayer(tms); 

		var lonLat = new OpenLayers.LonLat(116.40752599999996, 39.90403);  
		lonLat.transform(this.map.displayProjection, this.map.getProjectionObject());  
		this.map.setCenter(lonLat, 4);
	},
	this.attacks=function(){
		
		var fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
		var toProjection   = new OpenLayers.Projection("EPSG:900913"); // to Spherical Mercator Projection
		var destinationImg = "images/map/destination.png";
		var sourceImg = "images/map/source.png";
		var width = "14";
		var height = "19";
		var coordinateLayer = new OpenLayers.Layer.Vector("coordinateLayer");
		var lineLayer = new OpenLayers.Layer.Vector("lineLayer");
		var lineColor = "#0000FF";
		var lineRatio = 0.5;
		/**攻击源**/
		for(var i=0;i<this.sourceData.length;i++){
			var pos_s = new OpenLayers.Geometry.Point(this.sourceData[i]["longitude"], this.sourceData[i]["latitude"]).transform(fromProjection, toProjection);
			var desc = "<ul style='list-style:none;line-height:22px;font-size:12px;margin-left:-40px;width:300px;'>";
			var sources = [];
			for(var j=0;j<this.sourceData[i].ips.length;j++){
				for(var o in this.sourceData[i].ips[j]){
					desc += "<li>";
					desc += "<a href='#/search/"+o+"'>"+o+"</a> 发起攻击 "+this.sourceData[i].ips[j][o]+" 次";
					desc += "</li>";
				}
			}
			desc += "</ul>";
			
			var n = 0;
			/**记录攻击目的地**/
			for(var d=0;d<this.connectData.length;d++){
				for(var s=0;s<this.connectData[d].sources.length;s++){
					if(this.sourceData[i]["longitude"] == this.connectData[d].sources[s].longitude && this.sourceData[i]["latitude"] == this.connectData[d].sources[s].latitude){
						sources[n++] = {city:this.connectData[d]["city"],longitude:this.connectData[d]["longitude"],latitude:this.connectData[d]["latitude"],attacksNum:this.connectData[d].sources[s]["attacksNum"]}
					}
				}
			}
			var feature = new OpenLayers.Feature.Vector(
				pos_s,
				{
					description:'<div id="popup_title" style="font-size:12px;font-weight:bold;border-bottom:1px solid #CCC;padding-bottom:5px;">'+this.sourceData[i].city
						+'</div><div id="popup_desc">'+desc+'</div>',
					sources:sources,
					position:pos_s
				} ,
				{externalGraphic: sourceImg, graphicWidth: width, graphicHeight: height,cursor:'pointer'}
			);
			coordinateLayer.addFeatures(feature);
		}
		/**攻击目的地**/
		for(var i=0;i<this.destinationData.length;i++){
			var pos_d = new OpenLayers.Geometry.Point(this.destinationData[i]["longitude"], this.destinationData[i]["latitude"]).transform(fromProjection, toProjection);
			var sources;
			var desc = "<ul style='list-style:none;line-height:22px;font-size:12px;margin-left:-40px;width:300px;'>";
			for(var j=0;j<this.destinationData[i].servers.length;j++)
			for(var o in this.destinationData[i].servers[j]){
				desc += "<li>";
				desc += "<a href='javascript:"+o+"'>"+o+"</a> 受到攻击 "+this.destinationData[i].servers[j][o]+" 次";
				desc += "</li>";
			}
			desc += "</ul>";
			
			/**记录攻击源**/
			for(var j=0;j<this.connectData.length;j++){
				if(this.connectData[j].longitude == this.destinationData[i].longitude && this.connectData[j].latitude == this.destinationData[i].latitude){
					sources = this.connectData[j].sources;
					break;
				}
			}
			
			var feature = new OpenLayers.Feature.Vector(
				pos_d,
				{
					description:'<div id="popup_title" style="font-size:12px;font-weight:bold;border-bottom:1px solid #CCC;padding-bottom:5px;">'+this.destinationData[i].city
						+'</div><div id="popup_desc">'+desc+'</div>',
					sources:sources,
					position:pos_d
				} ,
				{externalGraphic: destinationImg, graphicWidth: width, graphicHeight: height,cursor:'pointer'}
			);
			coordinateLayer.addFeatures(feature);
		}
		/**连接线**/
		for(var i=0;i<this.connectData.length;i++){
			var pos_d = new OpenLayers.Geometry.Point(this.connectData[i]["longitude"], this.connectData[i]["latitude"]).transform(fromProjection, toProjection);
			for(var j=0;j<this.connectData[i].sources.length;j++){
				var pos_s = new OpenLayers.Geometry.Point(this.connectData[i].sources[j]["longitude"], this.connectData[i].sources[j]["latitude"]).transform(fromProjection, toProjection);
				lineLayer.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([pos_s, pos_d]),null,{
					strokeColor: lineColor,
					strokeOpacity: 0.5,
					strokeWidth:String(this.connectData[i].sources[j]["attacksNum"]).length*lineRatio
				})]);
			}
		}
		this.map.addLayer(coordinateLayer);
		this.map.addLayer(lineLayer);
		
		/**鼠标事件**/
		var controls = {
			selector:new OpenLayers.Control.SelectFeature(coordinateLayer, { 
				onSelect: feature_select, 
				onUnselect: feature_unselect,
				callbacks: {
					'over':feature_hover, 
					'out':feature_out
				}
			})
		};
		
		function feature_select(feature){
			lineLayer.removeAllFeatures();
			var source = feature.attributes.sources;
			var pos_d = feature.attributes.position;
			for(var j=0;j<source.length;j++){
				var pos_s = new OpenLayers.Geometry.Point(source[j]["longitude"], source[j]["latitude"]).transform(fromProjection, toProjection);
				lineLayer.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([pos_s, pos_d]),null,{
					strokeColor: lineColor,
					strokeOpacity: 0.5,
					strokeWidth:String(source[j]["attacksNum"]).length*lineRatio
				})]);
			}
			this.map.addLayer(lineLayer);
			
			feature.popup1 = new OpenLayers.Popup.FramedCloud("pop1",
				feature.geometry.getBounds().getCenterLonLat(),
				null,
				'<div class="markerContent">'+feature.attributes.description+'</div>',
				null,
				true,
				function() { controls['selector'].unselectAll(); }
			);
			this.map.addPopup(feature.popup1);
		}
		function feature_unselect(feature){
			if(feature.popup1){
				this.map.removePopup(feature.popup1);
				feature.popup1.destroy();
				feature.popup1 = null;
			}
		}
		
		function feature_hover(feature) {
			feature.popup = new OpenLayers.Popup.FramedCloud("pop",
				feature.geometry.getBounds().getCenterLonLat(),
				null,
				'<div class="markerContent">'+feature.attributes.description+'</div>',
				null,
				true,
				function() { controls['selector'].unselectAll(); }
			);
			var offset = {'size':new OpenLayers.Size(0,0),'offset':new OpenLayers.Pixel(5,-10)};
            feature.popup.anchor = offset;
			this.map.addPopup(feature.popup);
		}
		function feature_out(feature) {
			if(feature.popup){
				this.map.removePopup(feature.popup);
				feature.popup.destroy();
				feature.popup = null;
			}
		}
		this.map.addControl(controls['selector']);
		controls['selector'].activate();
	}
	
	this.sourceMap = function(){
		var fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
		var toProjection   = new OpenLayers.Projection("EPSG:900913"); // to Spherical Mercator Projection
		var sourceImg = "images/map/source.png";
		var width = "14";
		var height = "19";
		var coordinateLayer = new OpenLayers.Layer.Vector("coordinateLayer");
		/**攻击源**/
		for(var i=0;i<this.sourceData.length;i++){
			var pos_s = new OpenLayers.Geometry.Point(this.sourceData[i]["longitude"], this.sourceData[i]["latitude"]).transform(fromProjection, toProjection);
			var desc = "<ul style='list-style:none;line-height:22px;font-size:12px;margin-left:-40px;width:300px;'>";
			var sources = [];
			for(var j=0;j<this.sourceData[i].ips.length;j++){
				for(var o in this.sourceData[i].ips[j]){
					desc += "<li>";
					desc += "<a href='#/search/"+o+"'>"+o+"</a> 发起攻击 "+this.sourceData[i].ips[j][o]+" 次";
					desc += "</li>";
				}
			}
			desc += "</ul>";
			
			var n = 0;
			var feature = new OpenLayers.Feature.Vector(
				pos_s,
				{
					description:'<div id="popup_title" style="font-size:12px;font-weight:bold;border-bottom:1px solid #CCC;padding-bottom:5px;">'+this.sourceData[i].city
						+'</div><div id="popup_desc">'+desc+'</div>',
					sources:sources,
					position:pos_s
				} ,
				{externalGraphic: sourceImg, graphicWidth: width, graphicHeight: height,cursor:'pointer'}
			);
			coordinateLayer.addFeatures(feature);
		}
		this.map.addLayer(coordinateLayer);
		
		/**鼠标事件**/
		var controls = {
			selector:new OpenLayers.Control.SelectFeature(coordinateLayer, { 
				onSelect: feature_select, 
				onUnselect: feature_unselect,
				callbacks: {
					'over':feature_hover, 
					'out':feature_out
				}
			})
		};
		
		function feature_select(feature){
			var source = feature.attributes.sources;
			
			feature.popup1 = new OpenLayers.Popup.FramedCloud("pop1",
				feature.geometry.getBounds().getCenterLonLat(),
				null,
				'<div class="markerContent">'+feature.attributes.description+'</div>',
				null,
				true,
				function() { controls['selector'].unselectAll(); }
			);
			this.map.addPopup(feature.popup1);
		}
		function feature_unselect(feature){
			if(feature.popup1){
				this.map.removePopup(feature.popup1);
				feature.popup1.destroy();
				feature.popup1 = null;
			}
		}
		
		function feature_hover(feature) {
			feature.popup = new OpenLayers.Popup.FramedCloud("pop",
				feature.geometry.getBounds().getCenterLonLat(),
				null,
				'<div class="markerContent">'+feature.attributes.description+'</div>',
				null,
				true,
				function() { controls['selector'].unselectAll(); }
			);
			var offset = {'size':new OpenLayers.Size(0,0),'offset':new OpenLayers.Pixel(5,-10)};
            feature.popup.anchor = offset;
			this.map.addPopup(feature.popup);
		}
		function feature_out(feature) {
			if(feature.popup){
				this.map.removePopup(feature.popup);
				feature.popup.destroy();
				feature.popup = null;
			}
		}
		this.map.addControl(controls['selector']);
		controls['selector'].activate();
	}
}