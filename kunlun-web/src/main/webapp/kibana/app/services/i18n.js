define([
        'angular',
        'lodash'
    ],
    function(angular, _) {
        'use strict';

        var module = angular.module('kibana.services');

        module.service('i18n', function($resource) {
            this.translate = function($scope, language) {
                var languageFilePath = 'i18n_lang/' + language + '.json';
                $resource(languageFilePath).get(function(data) {
                    $scope.i18n = data;
                });
            };
        });
    });