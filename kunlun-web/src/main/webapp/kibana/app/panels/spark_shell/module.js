/** @scratch /panels/5
 *
 * include::panels/text.asciidoc[]
 */

/** @scratch /panels/text/0
 * == text
 * Status: *Stable*
 *
 * The text panel is used for displaying static text formated as markdown, sanitized html or as plain
 * text.
 *
 */
define([
  'jquery',
  'angular',
  'app',
  'lodash',
  'require',
  'socketio',
  './lib/jqconsole',
  'config',
],
function ($, angular, app, _, require, socketio, jqconsole, config) {
  'use strict';

  var module = angular.module('kibana.panels.spark_shell', []);
  app.useModule(module);

  module.controller('spark_shell', function($scope, querySrv, dashboard, filterSrv) {
    $scope.panelMeta = {
        editorTabs: [{
            title: '查询',
            src: 'app/partials/querySelect.html'
        }],
      status  : "Alpha",
      description : "Interactive Spark python shell"
    };

    // Set and populate defaults
    var _d = {
      queries: {
          mode: 'all',
          ids: []
      }
    };
    _.defaults($scope.panel,_d);

    $scope.init = function() {
      $scope.ready = false;

      var qconsole = $('#console').jqconsole('', '', '');

      qconsole.RegisterMatching('{', '}', 'brace');
      qconsole.RegisterMatching('(', ')', 'paran');
      qconsole.RegisterMatching('[', ']', 'bracket');

      var socket = socketio.connect(config.pyspark);

      socket.on('connect', function() {
        qconsole.Write('Connected to the remote Spark shell\n');
        socket.emit('shell', 'loadshell()');
      });
  
      socket.on('disconnect', function() {
        qconsole.Write('Disconnected\n');
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
              // get the index and all of the query filters
                var request = $scope.ejs.Request().indices(dashboard.indices);

                $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);

                var queries = querySrv.getQueryObjs($scope.panel.queries.ids);

                var boolQuery = $scope.ejs.BoolQuery();
                _.each(queries, function(q) {
                    boolQuery = boolQuery.should(querySrv.toEjsObj(q));
                });

               request = request.query(
                    $scope.ejs.FilteredQuery(
                        boolQuery,
                        filterSrv.getBoolFilter(filterSrv.ids())
                    ));

                command = command.substr(0, pos) + "create_rdd('" + config.elasticsearch + "','" + dashboard.indices + "','" + request.toString() + "')";
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

  });

});