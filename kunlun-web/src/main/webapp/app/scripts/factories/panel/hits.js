define(['app'],function(app){
	app.factory('PanelHits',['$http',function($http){
		return {
			generate:function(url,param,id_num,content){
				$http.post(url,param).success(function(data){
					var str = "<div class='panel_charts_hits' style='font-size:"+content.fontsize+"px'>";
					str += data.count;
					str += "</div>";
					$("#panel_content_"+id_num).html(str);
				});
			},
			generates:function(url,param,id_num,content,alt,panel){
				$http.post(url,param).success(function(data){
					
					var properties_name = "pro_";
					var panel_name = "charts_";
					
					$("#"+panel_name+panel.id+" .panel_code").attr("title",alt);
					$("#"+panel_name+panel.id+" .panel_code").attr("alt",alt);
					$("#"+panel_name+panel.id+" .panel_code").tooltipster({
						theme: 'tooltipster-shadow',
						contentAsHTML: true,
						interactive: true,
						position:'bottom',
						multiple: true
					});
					
					var obj = $("#"+panel_name+panel.id);
					obj.attr(properties_name+"id",panel.id);
					obj.attr(properties_name+"type",panel.type);
					obj.attr(properties_name+"content",panel.content);
					
					var str = "<div class='panel_charts_hits' style='font-size:"+content.fontsize+"px'>";
					str += data.count;
					str += "</div>";
					$("#panel_content_"+id_num).html(str);
				});
			}
		}
	}]);
});