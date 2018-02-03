/** @scratch /panels/5
 *
 * include::panels/histogram.asciidoc[]
 */

/** @scratch /panels/histogram/0
 *
 * == Histogram
 * Status: *Stable*
 *
 * The histogram panel allow for the display of time charts. It includes several modes and tranformations
 * to display event counts, mean, min, max and total of numeric fields, and derivatives of counter
 * fields.
 *
 */
define([
        'angular',
        'app',
        'jquery',
        'lodash',
        'kbn',
        'moment',
        './timeSeries',
        'numeral',
        'jquery.flot',
        'jquery.flot.events',
        'jquery.flot.selection',
        'jquery.flot.time',
        'jquery.flot.byte',
        'jquery.flot.stack',
        'jquery.flot.stackpercent'
    ],
    function(angular, app, $, _, kbn, moment, timeSeries, numeral) {

        'use strict';

        var module = angular.module('kibana.panels.forecast_histogram', []);
        app.useModule(module);

        module.controller('forecast_histogram', function($scope, querySrv, dashboard, filterSrv, $http) {
            $scope.panelMeta = {
                modals: [{
                    description: "查看",
                    icon: "icon-info-sign",
                    partial: "app/partials/inspector.html",
                    show: $scope.panel.spyable
                }],
                editorTabs: [{
                    title: '风格',
                    src: 'app/panels/forecast_histogram/styleEditor.html'
                }, {
                    title: '查询',
                    src: 'app/panels/forecast_histogram/queriesEditor.html'
                }, ],
                status: "正式版",
                description: "直方图"
            };

            // Set and populate defaults
            var _d = {
                /** @scratch /panels/histogram/3
                 *
                 * === Parameters
                 * ==== Axis options
                 * mode:: Value to use for the y-axis. For all modes other than count, +value_field+ must be
                 * defined. Possible values: count, mean, max, min, total.
                 */
                mode: 'count',
                /** @scratch /panels/histogram/3
                 * time_field:: x-axis field. This must be defined as a date type in Elasticsearch.
                 */
                time_field: '@timestamp',
                /** @scratch /panels/histogram/3
                 * value_field:: y-axis field if +mode+ is set to mean, max, min or total. Must be numeric.
                 */
                value_field: null,
                /** @scratch /panels/histogram/3
                 * x-axis:: Show the x-axis
                 */
                'x-axis': true,
                /** @scratch /panels/histogram/3
                 * y-axis:: Show the y-axis
                 */
                'y-axis': true,
                /** @scratch /panels/histogram/3
                 * scale:: Scale the y-axis by this factor
                 */
                scale: 1,
                /** @scratch /panels/histogram/3
                 * y_format:: 'none','bytes','short '
                 */
                y_format: 'none',
                /** @scratch /panels/histogram/5
                 * grid object:: Min and max y-axis values
                 * grid.min::: Minimum y-axis value
                 * grid.max::: Maximum y-axis value
                 */
                grid: {
                    max: null,
                    min: 0
                },
                /** @scratch /panels/histogram/5
                 *
                 * ==== Queries
                 * queries object:: This object describes the queries to use on this panel.
                 * queries.mode::: Of the queries available, which to use. Options: +all, pinned, unpinned, selected+
                 * queries.ids::: In +selected+ mode, which query ids are selected.
                 */
                queries: {
                    mode: 'all',
                    ids: []
                },
                /** @scratch /panels/histogram/3
                 * resolution:: If auto_int is true, shoot for this many bars.
                 */
                resolution: 100,
                /** @scratch /panels/histogram/3
                 * interval:: If auto_int is set to false, use this as the interval.
                 */
                interval: '5m',
                /** @scratch /panels/histogram/3
                 * interval:: Array of possible intervals in the *View* selector. Example [`auto',`1s',`5m',`3h']
                 */
                intervals: ['auto', '1s', '1m', '5m', '10m', '30m', '1h', '3h', '12h', '1d', '1w', '1y'],
                /** @scratch /panels/histogram/3
                 * ==== Drawing options
                 * lines:: Show line chart
                 */
                lines: false,
                /** @scratch /panels/histogram/3
                 * fill:: Area fill factor for line charts, 1-10
                 */
                fill: 0,
                /** @scratch /panels/histogram/3
                 * linewidth:: Weight of lines in pixels
                 */
                linewidth: 3,
                /** @scratch /panels/histogram/3
                 * points:: Show points on chart
                 */
                points: false,
                /** @scratch /panels/histogram/3
                 * pointradius:: Size of points in pixels
                 */
                pointradius: 5,
                /** @scratch /panels/histogram/3
                 * bars:: Show bars on chart
                 */
                bars: true,
                /** @scratch /panels/histogram/3
                 * stack:: Stack multiple series
                 */
                stack: false,
                /** @scratch /panels/histogram/3
                 * spyable:: Show inspect icon
                 */
                spyable: true,
                /** @scratch /panels/histogram/3
                 * zoomlinks:: Show `Zoom Out' link
                 */
                zoomlinks: true,

                forecast: true,

                /** @scratch /panels/histogram/3
                 * options:: Show quick view options section
                 */
                options: true,
                /** @scratch /panels/histogram/3
                 * legend:: Display the legond
                 */
                legend: true,
                /** @scratch /panels/histogram/3
                 * show_query:: If no alias is set, should the query be displayed?
                 */
                show_query: true,
                /** @scratch /panels/histogram/3
                 * interactive:: Enable click-and-drag to zoom functionality
                 */
                interactive: true,
                /** @scratch /panels/histogram/3
                 * legend_counts:: Show counts in legend
                 */
                legend_counts: true,
                /** @scratch /panels/histogram/3
                 * ==== Transformations
                 * timezone:: Correct for browser timezone?. Valid values: browser, utc
                 */
                timezone: 'browser', // browser or utc
                /** @scratch /panels/histogram/3
                 * percentage:: Show the y-axis as a percentage of the axis total. Only makes sense for multiple
                 * queries
                 */
                percentage: false,
                /** @scratch /panels/histogram/3
                 * zerofill:: Improves the accuracy of line charts at a small performance cost.
                 */
                zerofill: true,
                /** @scratch /panels/histogram/3
                 * derivative:: Show each point on the x-axis as the change from the previous point
                 */

                derivative: false,
                /** @scratch /panels/histogram/3
                 * tooltip object::
                 * tooltip.value_type::: Individual or cumulative controls how tooltips are display on stacked charts
                 * tooltip.query_as_alias::: If no alias is set, should the query be displayed?
                 */
                tooltip: {
                    value_type: 'cumulative',
                    query_as_alias: true
                }
            };

            _.defaults($scope.panel, _d);
            _.defaults($scope.panel.tooltip, _d.tooltip);
            _.defaults($scope.panel.grid, _d.grid);



            $scope.init = function() {
                // Hide view options by default
                $scope.options = false;

                // Always show the query if an alias isn't set. Users can set an alias if the query is too
                // long
                $scope.panel.tooltip.query_as_alias = true;

                $scope.get_data();

            };

            $scope.set_interval = function(interval) {
                $scope.panel.interval = interval;
            };

            $scope.interval_label = function(interval) {
                return interval;
            };

            /**
             * The time range effecting the panel
             * @return {[type]} [description]
             */
            $scope.get_time_range = function() {
                var range = $scope.range = filterSrv.timeRange('last');
                return range;
            };

            $scope.get_interval = function() {
                var interval = $scope.panel.interval,
                    range;
                $scope.panel.interval = interval || '10m';
                return $scope.panel.interval;
            };

            /**
             * Fetch the data for a chunk of a queries results. Multiple segments occur when several indicies
             * need to be consulted (like timestamped logstash indicies)
             *
             * The results of this function are stored on the scope's data property. This property will be an
             * array of objects with the properties info, time_series, and hits. These objects are used in the
             * render_panel function to create the historgram.
             *
             * @param {number} segment   The segment count, (0 based)
             * @param {number} query_id  The id of the query, generated on the first run and passed back when
             *                            this call is made recursively for more segments
             */
            $scope.get_data = function(data, segment, query_id) {
                var
                    _range,
                    _interval,
                    request,
                    queries,
                    results;

                if (_.isUndefined(segment)) {
                    segment = 0;
                }
                delete $scope.panel.error;

                // Make sure we have everything for the request to complete
                if (dashboard.indices.length === 0) {
                    return;
                }

                $scope.panel.forecast = false;

                _range = $scope.get_time_range();
                _interval = $scope.get_interval(_range);

                $scope.panelMeta.loading = true;

                request = $scope.ejs.Request().indices(dashboard.indices);
                // request.searchType("count");
                $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);
                queries = querySrv.getQueryObjs($scope.panel.queries.ids);

                // Build the query
                _.each(queries, function(q) {
                    var query = $scope.ejs.FilteredQuery(
                        querySrv.toEjsObj(q),
                        filterSrv.getBoolFilter(filterSrv.ids())
                    );

                    request = request
                        .aggregation($scope.ejs.FilterAggregation('histogram')
                            .agg($scope.ejs.DateHistogramAggregation('q_' + q.id)
                                .field($scope.panel.time_field)
                                .interval(_interval))
                            .filter($scope.ejs.QueryFilter(query))).size(0);
                });

                // Populate the inspector panel
                $scope.populate_modal(request);

                // Then run it
                results = request.doSearch();

                // Populate scope when we have results
                return results.then(function(results) {
                    $scope.panelMeta.loading = false;

                    $scope.legend = [];
                    $scope.hits = 0;
                    data = [];
                    query_id = $scope.query_id = new Date().getTime();

                    // Check for error and abort if found
                    if (!(_.isUndefined(results.error))) {
                        $scope.panel.error = $scope.parse_error(results.error);
                    }
                    // Make sure we're still on the same query/queries
                    else if ($scope.query_id === query_id) {

                        var i = 0,
                            time_series,
                            hits,
                            counters; // Stores the bucketed hit counts.

                        var data2forecast = new Array();

                        _.each(queries, function(q) {

                            var query_results = results.aggregations.histogram['q_' + q.id];

                            // we need to initialize the data variable on the first run,
                            // and when we are working on the first segment of the data.
                            var tsOpts = {
                                interval: _interval,
                                start_date: _range && _range.from,
                                end_date: _range && _range.to,
                                fill_style: $scope.panel.derivative ? 'null' : $scope.panel.zerofill ? 'minimal' : 'no'
                            };
                            time_series = new timeSeries.ZeroFilled(tsOpts);
                            hits = 0;
                            counters = {};

                            // push each entry into the time series, while incrementing counters
                            _.each(query_results.buckets, function(bucket) {
                                hits += bucket.doc_count; // The series level hits counter
                                $scope.hits += bucket.doc_count; // Entire dataset level hits counter
                                counters[bucket.key] = bucket.doc_count;

                                time_series.addValue(bucket.key, bucket.doc_count);

                                if (i == 0) {
                                    var record = {};
                                    record['date'] = bucket.key_as_string;
                                    record['value'] = bucket.doc_count;
                                    data2forecast.push(record);
                                }
                            });

                            $scope.legend[i] = {
                                query: q,
                                hits: hits
                            };

                            data[i] = {
                                info: q,
                                time_series: time_series,
                                hits: hits,
                                counters: counters
                            };

                            i++;
                        });
                    }

                    // request forecast service
                    var result = $http({
                        url: window.location.protocol + '//' + window.location.hostname + ':8888' + '/tsa',
                        method: "POST",
                        data: angular.toJson(data2forecast)
                    });

                    result.then(function(result) {
                        // we need to initialize the data variable on the first run,
                        // and when we are working on the first segment of the data.
                        var tsOpts = {
                            interval: _interval,
                            start_date: _range && _range.from,
                            end_date: _range && _range.to,
                            fill_style: $scope.panel.derivative ? 'null' : $scope.panel.zerofill ? 'minimal' : 'no'
                        }; {
                            time_series = new timeSeries.ZeroFilled(tsOpts);
                            hits = 0;
                            counters = {};
                            // push each entry into the time series, while incrementing counters
                            _.each(result.data.outliers, function(f) {
                                var key = moment(f.date).valueOf();
                                var n = Math.floor(f.value);

                                hits += n; // The series level hits counter
                                $scope.hits += n; // Entire dataset level hits counter
                                counters[key] = n;

                                time_series.addValue(key, n);
                            });

                            $scope.legend.push({
                                query: {
                                    alias: "异常值",
                                    color: "red"
                                },
                                hits: hits
                            });

                            data.push({
                                info: {
                                    alias: "异常值",
                                    color: "red"
                                },
                                time_series: time_series,
                                hits: hits,
                                counters: counters,
                            });
                        } {
                            time_series = new timeSeries.ZeroFilled(tsOpts);
                            hits = 0;
                            counters = {};
                            // push each entry into the time series, while incrementing counters
                            _.each(result.data.forecasts, function(f) {
                                var key = moment(f.date).valueOf();
                                var n = Math.floor(f.value);

                                hits += n; // The series level hits counter
                                $scope.hits += n; // Entire dataset level hits counter
                                counters[key] = n;

                                time_series.addValue(key, n);
                            });

                            $scope.legend.push({
                                query: {
                                    alias: "预测值",
                                    color: "gray"
                                },
                                hits: hits
                            });

                            data.push({
                                info: {
                                    alias: "预测值",
                                    color: "gray"
                                },
                                time_series: time_series,
                                hits: hits,
                                counters: counters,
                                isForecast: true
                            });
                        }
                        $scope.$emit('render', data);
                    });

                    $scope.$emit('render', data);
                });
            };

            // function $scope.zoom
            // factor :: Zoom factor, so 0.5 = cuts timespan in half, 2 doubles timespan
            $scope.zoom = function(factor) {
                var _range = filterSrv.timeRange('last');
                var _timespan = (_range.to.valueOf() - _range.from.valueOf());
                var _center = _range.to.valueOf() - _timespan / 2;

                var _to = (_center + (_timespan * factor) / 2);
                var _from = (_center - (_timespan * factor) / 2);

                // If we're not already looking into the future, don't.
                if (_to > Date.now() && _range.to < Date.now()) {
                    var _offset = _to - Date.now();
                    _from = _from - _offset;
                    _to = Date.now();
                }

                if (factor > 1) {
                    filterSrv.removeByType('time');
                }
                filterSrv.set({
                    type: 'time',
                    from: moment.utc(_from).toDate(),
                    to: moment.utc(_to).toDate(),
                    field: $scope.panel.time_field
                });
            };

            $scope.do_forecast = function() {


            }

            // I really don't like this function, too much dom manip. Break out into directive?
            $scope.populate_modal = function(request) {
                $scope.inspector = angular.toJson(JSON.parse(request.toString()), true);
            };

            $scope.set_refresh = function(state) {
                $scope.refresh = state;
            };

            $scope.close_edit = function() {
                if ($scope.refresh) {
                    $scope.get_data();
                }
                $scope.refresh = false;
                $scope.$emit('render');
            };

            $scope.render = function() {
                $scope.$emit('render');
            };

        });

        module.directive('forecastHistogramChart', function(dashboard, filterSrv, $http) {
            return {
                restrict: 'A',
                template: '<div></div>',
                link: function(scope, elem) {
                    var data, plot;

                    scope.$on('refresh', function() {
                        scope.get_data();
                    });

                    // Receive render events
                    scope.$on('render', function(event, d) {
                        data = d || data;
                        render_panel(data);
                    });

                    var scale = function(series, factor) {
                        return _.map(series, function(p) {
                            return [p[0], p[1] * factor];
                        });
                    };

                    var scaleSeconds = function(series, interval) {
                        return _.map(series, function(p) {
                            return [p[0], p[1] / kbn.interval_to_seconds(interval)];
                        });
                    };

                    var derivative = function(series) {
                        return _.map(series, function(p, i) {
                            var _v;
                            if (i === 0 || p[1] === null) {
                                _v = [p[0], null];
                            } else {
                                _v = series[i - 1][1] === null ? [p[0], null] : [p[0], p[1] - (series[i - 1][1])];
                            }
                            return _v;
                        });
                    };

                    // Function for rendering panel
                    function render_panel(data) {
                        // IE doesn't work without this
                        try {
                            elem.css({
                                height: scope.panel.height || scope.row.height
                            });
                        } catch (e) {
                            return;
                        }

                        // Populate from the query service
                        try {
                            _.each(data, function(series) {
                                series.label = series.info.alias;
                                series.color = series.info.color;
                            });
                        } catch (e) {
                            return;
                        }

                        // Set barwidth based on specified interval
                        var barwidth = kbn.interval_to_ms(scope.panel.interval);

                        var stack = scope.panel.stack ? true : null;

                        // Populate element
                        try {
                            var options = {
                                legend: {
                                    show: false
                                },
                                series: {
                                    stackpercent: scope.panel.stack ? scope.panel.percentage : false,
                                    stack: scope.panel.percentage ? null : stack,
                                    lines: {
                                        show: scope.panel.lines,
                                        // Silly, but fixes bug in stacked percentages
                                        fill: scope.panel.fill === 0 ? 0.001 : scope.panel.fill / 10,
                                        lineWidth: scope.panel.linewidth,
                                        steps: false
                                    },
                                    bars: {
                                        show: scope.panel.bars,
                                        fill: 1,
                                        barWidth: barwidth / 1.5,
                                        zero: false,
                                        lineWidth: 0
                                    },
                                    points: {
                                        show: scope.panel.points,
                                        fill: 1,
                                        fillColor: false,
                                        radius: scope.panel.pointradius
                                    },
                                    shadowSize: 1
                                },
                                yaxis: {
                                    show: scope.panel['y-axis'],
                                    min: scope.panel.grid.min,
                                    max: scope.panel.percentage && scope.panel.stack ? 100 : scope.panel.grid.max
                                },
                                xaxis: {
                                    timezone: scope.panel.timezone,
                                    show: scope.panel['x-axis'],
                                    mode: "time",
                                    min: _.isUndefined(scope.range.from) ? null : scope.range.from.getTime(),
                                    max: _.isUndefined(scope.range.to) ? null : scope.range.to.getTime(),
                                    timeformat: time_format(scope.panel.interval),
                                    label: "Datetime",
                                    ticks: elem.width() / 100
                                },
                                grid: {
                                    backgroundColor: null,
                                    borderWidth: 0,
                                    hoverable: true,
                                    color: '#c8c8c8'
                                }
                            };

                            if (scope.panel.y_format === 'bytes') {
                                options.yaxis.mode = "byte";
                                options.yaxis.tickFormatter = function(val, axis) {
                                    return kbn.byteFormat(val, 0, axis.tickSize);
                                };
                            }

                            if (scope.panel.y_format === 'short') {
                                options.yaxis.tickFormatter = function(val, axis) {
                                    return kbn.shortFormat(val, 0, axis.tickSize);
                                };
                            }


                            if (scope.panel.interactive) {
                                options.selection = {
                                    mode: "x",
                                    color: '#666'
                                };
                            }

                            // when rendering stacked bars, we need to ensure each point that has data is zero-filled
                            // so that the stacking happens in the proper order
                            var required_times = [];
                            if (data.length > 1) {
                                required_times = Array.prototype.concat.apply([], _.map(data, function(query) {
                                    return query.time_series.getOrderedTimes();
                                }));
                                required_times = _.uniq(required_times.sort(function(a, b) {
                                    // decending numeric sort
                                    return a - b;
                                }), true);
                            }

                            var data2plot = [];

                            for (var i = 0; i < data.length; i++) {
                                data[i].data = data[i].time_series.getFlotPairs(required_times);
                                if (data[i].isForecast) {
                                    data[i].lines = {
                                        show: true
                                    };
                                    data[i].bars = {
                                        show: false
                                    };
                                }
                            }

                            plot = $.plot(elem, data, options);

                        } catch (e) {
                            // Nothing to do here
                            var x = e;
                        }
                    }

                    function time_format(interval) {
                        var _int = kbn.interval_to_seconds(interval);
                        if (_int >= 2628000) {
                            return "%Y-%m";
                        }
                        if (_int >= 86400) {
                            return "%Y-%m-%d";
                        }
                        if (_int >= 60) {
                            return "%H:%M<br>%m-%d";
                        }

                        return "%H:%M:%S";
                    }

                    var $tooltip = $('<div>');
                    elem.bind("plothover", function(event, pos, item) {
                        var group, value, timestamp, interval;
                        interval = " per " + scope.panel.interval;
                        if (item) {
                            if (item.series.info.alias || scope.panel.tooltip.query_as_alias) {
                                group = '<small style="font-size:0.9em;">' +
                                    '<i class="icon-circle" style="color:' + item.series.color + ';"></i>' + ' ' +
                                    (item.series.info.alias || item.series.info.query) +
                                    '</small><br>';
                            } else {
                                group = kbn.query_color_dot(item.series.color, 15) + ' ';
                            }
                            value = (scope.panel.stack && scope.panel.tooltip.value_type === 'individual') ?
                                item.datapoint[1] - item.datapoint[2] :
                                item.datapoint[1];
                            if (scope.panel.y_format === 'bytes') {
                                value = kbn.byteFormat(value, 2);
                            }
                            if (scope.panel.y_format === 'short') {
                                value = kbn.shortFormat(value, 2);
                            } else {
                                value = numeral(value).format('0,0[.]000');
                            }
                            timestamp = scope.panel.timezone === 'browser' ?
                                moment(item.datapoint[0]).format('YYYY-MM-DD HH:mm:ss') :
                                moment.utc(item.datapoint[0]).format('YYYY-MM-DD HH:mm:ss');
                            $tooltip
                                .html(
                                    group + value + interval + " @ " + timestamp
                            )
                                .place_tt(pos.pageX, pos.pageY);
                        } else {
                            $tooltip.detach();
                        }
                    });

                    elem.bind("plotselected", function(event, ranges) {
                        filterSrv.set({
                            type: 'time',
                            from: moment.utc(ranges.xaxis.from).toDate(),
                            to: moment.utc(ranges.xaxis.to).toDate(),
                            field: scope.panel.time_field
                        });
                    });
                }
            };
        });
    });