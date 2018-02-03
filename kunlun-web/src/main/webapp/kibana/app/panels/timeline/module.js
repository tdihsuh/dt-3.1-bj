/** @scratch /panels/5
 *
 * include::panels/timeline.asciidoc[]
 */

/** @scratch /panels/timeline/0
 *
 * == timeline
 * Status: *Stable*
 *
 * The timeline panel allow for the display of time charts. It includes several modes and tranformations
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
        'jsonpath',
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

        var module = angular.module('kibana.panels.timeline', []);
        app.useModule(module);

        module.controller('timeline', function($rootScope, $scope, $modal, $q, $compile, $timeout, fields, querySrv, dashboard, filterSrv) {
            $scope.panelMeta = {
                modals: [{
                    description: "查看",
                    icon: "icon-info-sign",
                    partial: "app/partials/inspector.html",
                    show: $scope.panel.spyable
                }],
                editorTabs: [{
                    title: '风格',
                    src: 'app/panels/timeline/styleEditor.html'
                }, {
                    title: '查询',
                    src: 'app/panels/timeline/queriesEditor.html'
                }, {
                    title: '分页',
                    src: 'app/panels/table/pagination.html'
                }],
                status: "正式版",
                description: "时间轴图"
            };

            // Set and populate defaults
            var _d = {
                /** @scratch /panels/timeline/3
                 *
                 * === Parameters
                 * ==== Axis options
                 * mode:: Value to use for the y-axis. For all modes other than count, +value_field+ must be
                 * defined. Possible values: count, mean, max, min, total.
                 */
                mode: 'count',
                /** @scratch /panels/timeline/3
                 * time_field:: x-axis field. This must be defined as a date type in Elasticsearch.
                 */
                time_field: '@timestamp',
                /** @scratch /panels/timeline/3
                 * value_field:: y-axis field if +mode+ is set to mean, max, min or total. Must be numeric.
                 */
                value_field: null,
                /** @scratch /panels/timeline/3
                 * x-axis:: Show the x-axis
                 */
                'x-axis': true,
                /** @scratch /panels/timeline/3
                 * y-axis:: Show the y-axis
                 */
                'y-axis': true,
                /** @scratch /panels/timeline/3
                 * scale:: Scale the y-axis by this factor
                 */
                scale: 1,
                /** @scratch /panels/timeline/3
                 * y_format:: 'none','bytes','short '
                 */
                y_format: 'none',
                /** @scratch /panels/timeline/5
                 * grid object:: Min and max y-axis values
                 * grid.min::: Minimum y-axis value
                 * grid.max::: Maximum y-axis value
                 */
                grid: {
                    max: null,
                    min: 0
                },
                /** @scratch /panels/timeline/5
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

                queriesL2: {
                    mode: 'all',
                    ids: []
                },

                queriesL3: {
                    mode: 'all',
                    ids: []
                },

                /** @scratch /panels/timeline/3
                 *
                 * ==== Annotations
                 * annotate object:: A query can be specified, the results of which will be displayed as markers on
                 * the chart. For example, for noting code deploys.
                 * annotate.enable::: Should annotations, aka markers, be shown?
                 * annotate.query::: Lucene query_string syntax query to use for markers.
                 * annotate.size::: Max number of markers to show
                 * annotate.field::: Field from documents to show
                 * annotate.sort::: Sort array in format [field,order], For example [`@timestamp',`desc']
                 */
                annotate: {
                    enable: false,
                    query: "*",
                    size: 20,
                    field: '_type',
                    sort: ['_score', 'desc']
                },
                /** @scratch /panels/timeline/3
                 * ==== Interval options
                 * auto_int:: Automatically scale intervals?
                 */
                auto_int: true,
                /** @scratch /panels/timeline/3
                 * resolution:: If auto_int is true, shoot for this many bars.
                 */
                resolution: 100,
                /** @scratch /panels/timeline/3
                 * interval:: If auto_int is set to false, use this as the interval.
                 */
                interval: '5m',
                /** @scratch /panels/timeline/3
                 * interval:: Array of possible intervals in the *View* selector. Example [`auto',`1s',`5m',`3h']
                 */
                intervals: ['auto', '1s', '1m', '5m', '10m', '30m', '1h', '3h', '12h', '1d', '1w', '1y'],
                /** @scratch /panels/timeline/3
                 * ==== Drawing options
                 * lines:: Show line chart
                 */
                lines: false,
                /** @scratch /panels/timeline/3
                 * fill:: Area fill factor for line charts, 1-10
                 */
                fill: 0,
                /** @scratch /panels/timeline/3
                 * linewidth:: Weight of lines in pixels
                 */
                linewidth: 3,
                /** @scratch /panels/timeline/3
                 * points:: Show points on chart
                 */
                points: false,
                /** @scratch /panels/timeline/3
                 * pointradius:: Size of points in pixels
                 */
                pointradius: 5,
                /** @scratch /panels/timeline/3
                 * bars:: Show bars on chart
                 */
                bars: true,
                /** @scratch /panels/timeline/3
                 * stack:: Stack multiple series
                 */
                stack: true,
                                /** @scratch /panels/table/5
                 * === Parameters
                 *
                 * size:: The number of hits to show per page
                 */
                size: 100, // Per page
                /** @scratch /panels/table/5
                 * pages:: The number of pages available
                 */
                pages: 5, // Pages available
                /** @scratch /panels/table/5
                 * offset:: The current page
                 */
                offset: 0,
                /** @scratch /panels/table/5
                 * sort:: An array describing the sort order of the table. For example [`@timestamp',`desc']
                 */
                sort: ['_score', 'desc'],
                /** @scratch /panels/table/5
                 * overflow:: The css overflow property. `min-height' (expand) or `auto' (scroll)
                 */
                overflow: 'min-height',
                /** @scratch /panels/table/5
                 * fields:: the fields used a columns of the table, in an array.
                 */
                fields: [],
                /** @scratch /panels/table/5
                 * highlight:: The fields on which to highlight, in an array
                 */
                highlight: [],
                /** @scratch /panels/table/5
                 * sortable:: Set sortable to false to disable sorting
                 */
                sortable: true,
                /** @scratch /panels/table/5
                 * header:: Set to false to hide the table column names
                 */
                header: true,
                /** @scratch /panels/table/5
                 * paging:: Set to false to hide the paging controls of the table
                 */
                paging: true,
                /** @scratch /panels/table/5
                 * field_list:: Set to false to hide the list of fields. The user will be able to expand it,
                 * but it will be hidden by default
                 */
                field_list: true,
                /** @scratch /panels/table/5
                 * all_fields:: Set to true to show all fields in the mapping, not just the current fields in
                 * the table.
                 */
                all_fields: false,
                /** @scratch /panels/table/5
                 * trimFactor:: The trim factor is the length at which to truncate fields takinging into
                 * consideration the number of columns in the table. For example, a trimFactor of 100, with 5
                 * columns in the table, would trim each column at 20 character. The entirety of the field is
                 * still available in the expanded view of the event.
                 */
                trimFactor: 300,
                /** @scratch /panels/table/5
                 * localTime:: Set to true to adjust the timeField to the browser's local time
                 */
                localTime: false,
                /** @scratch /panels/table/5
                 * timeField:: If localTime is set to true, this field will be adjusted to the browsers local time
                 */
                timeField: '@timestamp',
                /** @scratch /panels/timeline/3
                 * spyable:: Show inspect icon
                 */
                spyable: true,
                /** @scratch /panels/timeline/3
                 * zoomlinks:: Show `Zoom Out' link
                 */
                zoomlinks: true,
                /** @scratch /panels/timeline/3
                 * options:: Show quick view options section
                 */
                options: true,
                /** @scratch /panels/timeline/3
                 * legend:: Display the legond
                 */
                legend: true,
                /** @scratch /panels/timeline/3
                 * show_query:: If no alias is set, should the query be displayed?
                 */
                show_query: true,
                /** @scratch /panels/timeline/3
                 * interactive:: Enable click-and-drag to zoom functionality
                 */
                interactive: true,
                /** @scratch /panels/timeline/3
                 * legend_counts:: Show counts in legend
                 */
                legend_counts: true,
                /** @scratch /panels/timeline/3
                 * ==== Transformations
                 * timezone:: Correct for browser timezone?. Valid values: browser, utc
                 */
                timezone: 'browser', // browser or utc
                /** @scratch /panels/timeline/3
                 * percentage:: Show the y-axis as a percentage of the axis total. Only makes sense for multiple
                 * queries
                 */
                percentage: false,
                /** @scratch /panels/timeline/3
                 * zerofill:: Improves the accuracy of line charts at a small performance cost.
                 */
                zerofill: true,
                /** @scratch /panels/timeline/3
                 * derivative:: Show each point on the x-axis as the change from the previous point
                 */

                derivative: false,
                /** @scratch /panels/timeline/3
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
            _.defaults($scope.panel.annotate, _d.annotate);
            _.defaults($scope.panel.grid, _d.grid);

            $scope.init = function() {
                $scope.columns = {};
                _.each($scope.panel.fields, function(field) {
                    $scope.columns[field] = true;
                });

                $scope.Math = Math;
                $scope.identity = angular.identity;
                $scope.$on('refresh', function() {
                    $scope.get_table_data();
                });

                $scope.fields = fields;
                $scope.get_table_data();                

                // Hide view options by default
                $scope.options = false;

                // Always show the query if an alias isn't set. Users can set an alias if the query is too
                // long
                $scope.panel.tooltip.query_as_alias = true;

                $scope.get_data();
            };

            // Create a percent function for the view
            $scope.percent = kbn.to_percent;

            $scope.closeFacet = function() {
                if ($scope.modalField) {
                    delete $scope.modalField;
                }
            };

            $scope.termsModal = function(field, chart) {
                $scope.closeFacet();
                $timeout(function() {
                    $scope.modalField = field;
                    $scope.adhocOpts = {
                        height: "200px",
                        chart: chart,
                        field: field,
                        span: $scope.panel.span,
                        type: 'terms',
                        title: 'Top 10 terms in field ' + field
                    };
                    showModal(
                        angular.toJson($scope.adhocOpts), 'terms');
                }, 0);
            };

            $scope.statsModal = function(field) {
                $scope.closeFacet();
                $timeout(function() {
                    $scope.modalField = field;
                    $scope.adhocOpts = {
                        height: "200px",
                        field: field,
                        mode: 'mean',
                        span: $scope.panel.span,
                        type: 'stats',
                        title: 'Statistics for ' + field
                    };
                    showModal(
                        angular.toJson($scope.adhocOpts), 'stats');
                }, 0);
            };

            var showModal = function(panel, type) {
                $scope.facetPanel = panel;
                $scope.facetType = type;
            };

            $scope.toggle_micropanel = function(field, groups) {
                var docs = _.map($scope.data, function(_d) {
                    return _d.kibana._source;
                });
                var topFieldValues = kbn.top_field_values(docs, field, 10, groups);
                $scope.micropanel = {
                    field: field,
                    grouped: groups,
                    values: topFieldValues.counts,
                    hasArrays: topFieldValues.hasArrays,
                    related: kbn.get_related_fields(docs, field),
                    limit: 10,
                    count: _.countBy(docs, function(doc) {
                        return _.contains(_.keys(doc), field);
                    })['true']
                };


                var nodeInfo = $scope.ejs.client.get('/' + dashboard.indices + '/_mapping/field/' + field,
                    undefined, undefined, function(data, status) {
                        console.log(status);
                        return;
                    });

                return nodeInfo.then(function(p) {
                    var types = _.uniq(jsonPath(p, '*.*.*.*.mapping.*.type'));
                    if (_.isArray(types)) {
                        $scope.micropanel.type = types.join(', ');
                    }


                    if (_.intersection(types, ['long', 'float', 'integer', 'double']).length > 0) {
                        $scope.micropanel.hasStats = true;
                    }
                });

            };

            $scope.micropanelColor = function(index) {
                var _c = ['bar-success', 'bar-warning', 'bar-danger', 'bar-info', 'bar-primary'];
                return index > _c.length ? '' : _c[index];
            };

            $scope.set_sort = function(field) {
                if ($scope.panel.sort[0] === field) {
                    $scope.panel.sort[1] = $scope.panel.sort[1] === 'asc' ? 'desc' : 'asc';
                } else {
                    $scope.panel.sort[0] = field;
                }
                $scope.get_table_data();
            };

            $scope.toggle_field = function(field) {
                if (_.indexOf($scope.panel.fields, field) > -1) {
                    $scope.panel.fields = _.without($scope.panel.fields, field);
                    delete $scope.columns[field];
                } else {
                    $scope.panel.fields.push(field);
                    $scope.columns[field] = true;
                }
            };

            $scope.toggle_highlight = function(field) {
                if (_.indexOf($scope.panel.highlight, field) > -1) {
                    $scope.panel.highlight = _.without($scope.panel.highlight, field);
                } else {
                    $scope.panel.highlight.push(field);
                }
            };

            $scope.toggle_details = function(row) {
                row.kibana.details = row.kibana.details ? false : true;
                row.kibana.view = row.kibana.view || 'table';
                //row.kibana.details = !row.kibana.details ? $scope.without_kibana(row) : false;
            };

            $scope.page = function(page) {
                $scope.panel.offset = page * $scope.panel.size;
                $scope.get_table_data();
            };

            $scope.build_search = function(field, value, negate) {
                var query;
                // This needs to be abstracted somewhere
                if (_.isArray(value)) {
                    query = "(" + _.map(value, function(v) {
                        return angular.toJson(v);
                    }).join(" AND ") + ")";
                } else if (_.isUndefined(value)) {
                    query = '*';
                    negate = !negate;
                } else {
                    query = angular.toJson(value);
                }
                $scope.panel.offset = 0;
                filterSrv.set({
                    type: 'field',
                    field: field,
                    query: query,
                    mandate: (negate ? 'mustNot' : 'must')
                });
            };

            $scope.fieldExists = function(field, mandate) {
                filterSrv.set({
                    type: 'exists',
                    field: field,
                    mandate: mandate
                });
            };

            $scope.get_table_data = function(segment, query_id) {
                var
                    _segment,
		    request,
                    boolQuery,
                    queries,
                    sort;

                $scope.panel.error = false;

                // Make sure we have everything for the request to complete
                if (dashboard.indices.length === 0) {
                    return;
                }

                sort = [$scope.ejs.Sort($scope.panel.sort[0]).order($scope.panel.sort[1]).ignoreUnmapped(true)];
                if ($scope.panel.localTime) {
                    sort.push($scope.ejs.Sort($scope.panel.timeField).order($scope.panel.sort[1]).ignoreUnmapped(true));
                }

                $scope.panelMeta.loading = true;

                _segment = _.isUndefined(segment) ? 0 : segment;
		//$scope.segment = _segment;

                request = $scope.ejs.Request().indices(dashboard.indices[_segment]);

                $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);
                queries = querySrv.getQueryObjs($scope.panel.queries.ids);

                var boolFilter = filterSrv.getBoolFilter(filterSrv.ids());

                var queriesL2 = querySrv.getQueryObjs($scope.panel.queriesL2.ids);

                _.each(queriesL2, function(q) {
                    if (_.contains($scope.panel.queriesL3.ids, q.id))
                        boolFilter.should($scope.ejs.QueryFilter($scope.ejs.QueryStringQuery(q.query)));                  
                });

                var firstQuery = $scope.ejs.FilteredQuery(
                    querySrv.toEjsObj(queries[0]),
                    boolFilter
                ); 

                request = request.query(firstQuery)
                    .highlight(
                        $scope.ejs.Highlight($scope.panel.highlight)
                        .fragmentSize(2147483647) // Max size of a 32bit unsigned int
                        .preTags('@start-highlight@')
                        .postTags('@end-highlight@')
                    ).size($scope.panel.size * $scope.panel.pages)
                    .sort(sort);

                $scope.populate_modal(request);

                // Populate scope when we have results
                request.doSearch().then(function(results) {
                    $scope.panelMeta.loading = false;

                    if (_segment === 0) {
                        $scope.panel.offset = 0;
                        $scope.hits = 0;
                        $scope.data = [];
                        $scope.current_fields = [];
                        query_id = $scope.query_id = new Date().getTime();
                    }

                    // Check for error and abort if found
                    if (!(_.isUndefined(results.error))) {
                        $scope.panel.error = $scope.parse_error(results.error);
                        return;
                    }

                    // Check that we're still on the same query, if not stop
                    if ($scope.query_id === query_id) {

                        // This is exceptionally expensive, especially on events with a large number of fields
                        $scope.data = $scope.data.concat(_.map(results.hits.hits, function(hit) {
                            var
                                _h = _.clone(hit),
                                _p = _.omit(hit, '_source', 'sort', '_score');

                            // _source is kind of a lie here, never display it, only select values from it
                            _h.kibana = {
                                _source: _.extend(kbn.flatten_json(hit._source), _p),
                                highlight: kbn.flatten_json(hit.highlight || {})
                            };

                            // Kind of cheating with the _.map here, but this is faster than kbn.get_all_fields
                            $scope.current_fields = $scope.current_fields.concat(_.keys(_h.kibana._source));

                            return _h;
                        }));

                        $scope.current_fields = _.uniq($scope.current_fields);
                        $scope.hits += results.hits.total;

                        // Sort the data
                        $scope.data = _.sortBy($scope.data, function(v) {
                            if (!_.isUndefined(v.sort)) {
                                return v.sort[0];
                            } else {
                                return v._score;
                            }
                        });

                        // Reverse if needed
                        if ($scope.panel.sort[1] === 'desc') {
                            $scope.data.reverse();
                        }

                        // Keep only what we need for the set
                        $scope.data = $scope.data.slice(0, $scope.panel.size * $scope.panel.pages);

                    } else {
                        return;
                    }

                    // If we're not sorting in reverse chrono order, query every index for
                    // size*pages results
                    // Otherwise, only get size*pages results then stop querying
                    if (($scope.data.length < $scope.panel.size * $scope.panel.pages ||
                            !((_.contains(filterSrv.timeField(), $scope.panel.sort[0])) && $scope.panel.sort[1] === 'desc')) &&
                        _segment + 1 < dashboard.indices.length) {
                        $scope.get_table_data(_segment+1, $scope.query_id);
                    }

                });
            };

            $scope.without_kibana = function(row) {
                var _c = _.clone(row);
                delete _c.kibana;
                return _c;
            };

            $scope.locate = function(obj, path) {
                path = path.split('.');
                var arrayPattern = /(.+)\[(\d+)\]/;
                for (var i = 0; i < path.length; i++) {
                    var match = arrayPattern.exec(path[i]);
                    if (match) {
                        obj = obj[match[1]][parseInt(match[2], 10)];
                    } else {
                        obj = obj[path[i]];
                    }
                }
                return obj;
            };

            $scope.set_interval = function(interval) {
                if (interval !== 'auto') {
                    $scope.panel.auto_int = false;
                    $scope.panel.interval = interval;
                } else {
                    $scope.panel.auto_int = true;
                }
            };

            $scope.interval_label = function(interval) {
                return $scope.panel.auto_int && interval === $scope.panel.interval ? interval + " (auto)" : interval;
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
                if ($scope.panel.auto_int) {
                    range = $scope.get_time_range();
                    if (range) {
                        interval = kbn.secondsToHms(
                            kbn.calculate_interval(range.from, range.to, $scope.panel.resolution, 0) / 1000
                        );
                    }
                }
                $scope.panel.interval = interval || '1h';
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
                _range = $scope.get_time_range();
                _interval = $scope.get_interval(_range);

                if ($scope.panel.auto_int) {
                    $scope.panel.interval = kbn.secondsToHms(
                        kbn.calculate_interval(_range.from, _range.to, $scope.panel.resolution, 0) / 1000);
                }

                $scope.panelMeta.loading = true;

                request = $scope.ejs.Request().indices(dashboard.indices);
                
                $scope.panel.queries.ids = querySrv.idsByMode($scope.panel.queries);
                queries = querySrv.getQueryObjs($scope.panel.queries.ids);

                var firstQuery = $scope.ejs.FilteredQuery(
                        querySrv.toEjsObj(queries[0]),
                        filterSrv.getBoolFilter(filterSrv.ids())
                    ); 

                var queriesL2 = querySrv.getQueryObjs($scope.panel.queriesL2.ids);
                var queriesL2ES = {};
                _.each(queriesL2, function(q) {
                    if (_.contains($scope.panel.queriesL3.ids, q.id))
                        queriesL2ES['q_' + q.id] = {"query" : $scope.ejs.QueryStringQuery(q.query)};
                });    

                request = request
                    .aggregation($scope.ejs.FilterAggregation('timeline')
                        .agg($scope.ejs.FiltersAggregation('y_axis')
                                .filters(queriesL2ES).agg(
                                    $scope.ejs.DateHistogramAggregation('x_axis')
                                        .field($scope.panel.time_field)
                                        .interval(_interval)
                                ))
                        .filter($scope.ejs.QueryFilter(firstQuery)));

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

                        _.each(queriesL2, function(q) {
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
                            var query_results = results.aggregations.timeline.y_axis.buckets['q_' + q.id];
                            if (!_.isUndefined(query_results)) {
                                _.each(query_results.x_axis.buckets, function(bucket) {
                                    hits += bucket.doc_count; // The series level hits counter
                                    $scope.hits += bucket.doc_count; // Entire dataset level hits counter
                                    counters[bucket.key] = bucket.doc_count;

                                    time_series.addValue(bucket.key, bucket.doc_count);

                                });
                            }    

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

            // I really don't like this function, too much dom manip. Break out into directive?
            $scope.populate_modal = function(request) {
                $scope.inspector = angular.toJson(JSON.parse(request.toString()), true);
            };

            $scope.set_refresh = function(state) {
                $scope.refresh = state;
            };

            $scope.close_edit = function() {
                if ($scope.refresh) {
                    $scope.get_table_data();
                    $scope.get_data();
                }
                $scope.columns = [];
                _.each($scope.panel.fields, function(field) {
                    $scope.columns[field] = true;
                });
                $scope.refresh = false;
                $scope.$emit('render');
            };

            $scope.render = function() {
                $scope.$emit('render');
            };

        });

        // This also escapes some xml sequences
        module.filter('tableHighlight', function() {
            return function(text) {
                if (!_.isUndefined(text) && !_.isNull(text) && text.toString().length > 0) {
                    return text.toString().
                    replace(/&/g, '&amp;').
                    replace(/</g, '&lt;').
                    replace(/>/g, '&gt;').
                    replace(/\r?\n/g, '<br/>').
                    replace(/@start-highlight@/g, '<code class="highlight">').
                    replace(/@end-highlight@/g, '</code>');
                }
                return '';
            };
        });

        module.filter('tableTruncate', function() {
            return function(text, length, factor) {
                if (!_.isUndefined(text) && !_.isNull(text) && text.toString().length > 0) {
                    return text.length > length / factor ? text.substr(0, length / factor) + '...' : text;
                }
                return '';
            };
        });

        module.filter('tableJson', function() {
            var json;
            return function(text, prettyLevel) {
                if (!_.isUndefined(text) && !_.isNull(text) && text.toString().length > 0) {
                    json = angular.toJson(text, prettyLevel > 0 ? true : false);
                    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    if (prettyLevel > 1) {
                        /* jshint maxlen: false */
                        json = json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
                            var cls = 'number';
                            if (/^"/.test(match)) {
                                if (/:$/.test(match)) {
                                    cls = 'key strong';
                                } else {
                                    cls = '';
                                }
                            } else if (/true|false/.test(match)) {
                                cls = 'boolean';
                            } else if (/null/.test(match)) {
                                cls = 'null';
                            }
                            return '<span class="' + cls + '">' + match + '</span>';
                        });
                    }
                    return json;
                }
                return '';
            };
        });

        // WIP
        module.filter('tableLocalTime', function() {
            return function(text, event) {
                return moment(event.sort[1]).format("YYYY-MM-DDTHH:mm:ss.SSSZ");
            };
        });

        module.directive('timelineChart', function(dashboard, filterSrv) {
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

                            if (scope.panel.annotate.enable) {
                                options.events = {
                                    clustering: true,
                                    levels: 1,
                                    data: scope.annotations,
                                    types: {
                                        'annotation': {
                                            level: 1,
                                            icon: {
                                                width: 20,
                                                height: 21,
                                                icon: "timeline-marker"
                                            }
                                        }
                                    }
                                    //xaxis: int    // the x axis to attach events to
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


                            for (var i = 0; i < data.length; i++) {
                                var _d = data[i].time_series.getFlotPairs(required_times);
                                if (scope.panel.derivative) {
                                    _d = derivative(_d);
                                }
                                if (scope.panel.scale !== 1) {
                                    _d = scale(_d, scope.panel.scale);
                                }
                                if (scope.panel.scaleSeconds) {
                                    _d = scaleSeconds(_d, scope.panel.interval);
                                }
                                data[i].data = _d;
                            }

                            plot = $.plot(elem, data, options);

                        } catch (e) {
                            // Nothing to do here
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
                        interval = " 每 " + (scope.panel.scaleSeconds ? '1s' : scope.panel.interval);
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
