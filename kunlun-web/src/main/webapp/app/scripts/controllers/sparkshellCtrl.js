define(['app','socketio','../jqconsole/jqconsole'],function(app,socketio, jqconsole){
	app.controller('sparkshellCtrl',['$scope','$http','hs_es','$rootScope',function($scope,$http,hs_es,$rootScope){
	  $scope.init = function() {
      $scope.ready = false;
      var pro_content =eval("("+$('#charts_'+$rootScope.sparkPanelId).attr('pro_content')+")");
      if (pro_content==undefined) {
    	  pro_content = eval("("+$('#charts_'+$rootScope.panel.virtualNum).attr('pro_content')+")");
      }
	  var pro_startime = pro_content.startime;
	  var timeFrom = new Date(pro_startime.replace(/-/g,"/")).getTime(); 
	  var pro_endtime = pro_content.endtime;
	  var timeEnd = new Date(pro_endtime.replace(/-/g,"/")).getTime();
	  var pro_condition = pro_content.condition;
	  $scope.sparkshell = {
			//ANOMALY,logs_20131208,......
			"indices" : "logs_*",	
			/*{"query":{"filtered":{"query":{"bool":{"should":[{"query_string":{"query":"_type:weblogs"}}]
			 *                                       }
			 *                              },
			 * 						"filter":{"bool":{"must":[{"range":{"datetime":{"from":1386518461000,"to":1387641599000}}}]
			 *                                       }
			 *                              }
			 *                     }
			 *         }
			 *}
			*/
			"request" : "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"should\":[{\"query_string\":{\"query\":\""+pro_condition+"\"}}]}},\"filter\":{\"bool\":{\"must\":[{\"range\":{\"datetime\":{\"from\":"+timeFrom+",\"to\":"+timeEnd+"}}}]}}}}}"	
		};	
		

      var qconsole = $('#sparkshellconsole').jqconsole('', '', '');

      qconsole.RegisterMatching('{', '}', 'brace');
      qconsole.RegisterMatching('(', ')', 'paran');
      qconsole.RegisterMatching('[', ']', 'bracket');

      var socket = socketio.connect("http://" + window.location.hostname + ":10010/pyspark");

      socket.on('connect', function() {
    	  //alert($scope.ready );
        qconsole.Write('Connected to the remote Spark shell\n');
        socket.emit('shell', 'loadshell()');
      });
      
      socket.on('message', function (message, callback) {
    	  qconsole.Write('message\n');
      });
      
      socket.on('connect_failed', function () {
    	  qconsole.Write('connect_failed\n');
      });
      socket.on('error', function () {
    	  qconsole.Write('error\n');
      });
      socket.on('reconnect_failed', function () {
    	  qconsole.Write('reconnect_failed\n');
      });
      socket.on('reconnect', function () {
    	  qconsole.Write('reconnect\n');
      });
      
  
      socket.on('disconnect', function() {
        qconsole.Write('Disconnected\n');
       // socket.emit('shell', 'loadshell()');
      });

      String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
      };

      socket.on('shell', function(data, ackServerCallback) {
        var responseEnded = false;
        if (data.endsWith("#END#")) {
          responseEnded = true;  
          data = data.substring(0, data.length - "#END#".length);
        }

        qconsole.Write(data);

        if (responseEnded)
          startPrompt();
      });

      var startPrompt = function () {
        // Start the prompt with history enabled.
        qconsole.Prompt(true, function (command) {
          // Output input with the class jqconsole-output.
          try {
            var pos = command.indexOf("create_rdd");
            if (pos >= 0) {
                command = command.substr(0, pos) + "create_rdd('" + hs_es.spark_name + "','" + $scope.sparkshell.indices + "','" + $scope.sparkshell.request + "')";
            }  

            socket.emit('shell', command, function(arg1) {});
          } catch (e) {
            qconsole.Write('ERROR: ' + e.message + '\n');
          }
          // Restart the prompt.
          startPrompt();
        });
      };
    }; 

	}]);
})