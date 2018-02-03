define([
        'angular',
        'config',
        'lodash',
        'services/i18n'
    ],
    function(angular, config, _) {
        "use strict";

        var module = angular.module('kibana.controllers');

        module.controller('i18n_ctrl', function($scope, i18n) {
            i18n.translate($scope, 'zh_cn');
        });
    });