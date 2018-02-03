define(['app'],function(app){
	app.factory('PanelService',['$http',function($http){
		return {
			buildPanel:function(panelDiv,properties){
				
				var saveId = "save_panel_";
				var properties_name = "pro_";
				
				var panelId = properties.panelId;
				var n = panelId.substring(panelId.lastIndexOf("_")+1,panelId.length);
				var save_panel_id = saveId + n;
				
				panelDiv.attr("style","width:"+properties.content.width);
				panelDiv.attr(properties_name+"id",n);
				panelDiv.attr(properties_name+"type",properties.panelType);
				panelDiv.attr(properties_name+"content",JSON.stringify(properties.content));
				
				var str = "";
				str += "<div class='panel_title hs_title'>";
				str += "<div class='panel_title_txt'>"+properties.content.title+"</div>";
				str += "<div class='panel_title_icon'>";
				str += "<i class='fa fa-info-circle hs_cursor panel_code'></i>";
				if(properties.save){
					str += "<i class='fa fa-save hs_cursor' id='"+save_panel_id+"' title='保存' ng-click='saveToDB(\""+panelId+"\",\""+save_panel_id+"\")'></i>";
				}
				str += "<i class='fa fa-cog hs_cursor' title='配置' ng-click='addPanel(\""+panelId+"\")'></i>";
				str += "<i class='fa fa-times hs_cursor' title='删除' ng-click='removePanel(\""+n+"\")'></i>";
				str += "</div>";
				str += "</div>";
				str += "<div id='panel_content_"+n+"' class='hs_content panel_content' style='height:"+properties.content.height+"px'></div>";
				str += "<div class='hs_bottom panel_bottom'></div>";
				
				panelDiv.html(str);
			}
		}
	}]);
});