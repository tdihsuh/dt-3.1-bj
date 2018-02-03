define([ 'app' ], function(app) {
	app.value('hs_auto_refresh',10000);
	app.value("hs_map_url", "map");
	app.value('hs_es',{
		"host_name":"proxy",
		"spark_name":"localhost:9200",
		"log_index_name":"logs_",
		"event_index_name":"logs_",
		"log_type_name":"log_*,event_*",
		"event_type_name":"event_*",
		"anormal_type_name":"anomaly",
		"model_index_name":"model",
		"manual_model_name":"manual",
		"model_type_404":"status_404"
	});
	//app.value('hs_color',["#7EB26D","#EAB839","#6ED0E0","#EF843C","#E24D42","#1F78C1","#BA43A9","#705DA0","#508642","#CCA300","#444444"]);
//	app.value('hs_color',["#e9d69e","#dab861","#e1a947","#EF843C","#E24D42","#c25571","#BA43A9","#705DA0","#508642","#CCA300","#444444"]);
	app.value('hs_color',["#A4C400","#60A917","#008A00","#00ABA9","#1BA1E2","#0050EF","#6A00FF","#AA00FF","#F472D0","#D80073","#A20025","#E51400","#FA6800","#F0A30A","#FFE826","#825A2C","#6D8764","#647687","#76608A","#87794E"]);
	//app.value('hs_color',["#d6ae41","#d69241","#d68041","#d66b41","#d64c41","#d64141","#e63e6e","#e63e8d","#d950aa","#d766cb","#b666d7",]);
	//app.value('hs_color',["#d1d1d1","#bababa","#9f9f9f","#919191","#838383","#737373","#646464","#535353","#414141","#313131","#242424"]);
	//app.value('hs_color',["#fbdcdc","#f8c4c4","#f4acac","#ee9494","#e57a7a","#dd6262","#d14e4e","#c53939","#b62929","#a91b1b","#9c0d0d"]);
	app.value('hs_info',{
		"success":"操作成功！",
		"errors":"出错了！"
	});
	app.value('hs_pie_chart_id','hs_pie_chart_');
});