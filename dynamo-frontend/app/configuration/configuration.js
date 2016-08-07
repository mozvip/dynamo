'use strict';

angular.module('dynamo.configuration', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/configuration', {
            templateUrl: 'configuration/configuration.html',
            controller: 'ConfigurationCtrl'
        }).when('/quality-profiles', {
            templateUrl: 'configuration/quality-profiles.html',
            controller: 'QualityProfilesCtrl'
        });
    }])

    .controller('ConfigurationCtrl', ['$scope', 'configurationService', '$filter', function ($scope, configurationService, $filter) {

        $scope.items = {};

        configurationService.getItems().then(function (response) {
            $scope.items = response.data;
        });

        $scope.configurationChanged = function (key) {
            configurationService.set(key, $scope.items[key].value);
        }

    }])

    .controller('QualityProfilesCtrl', ['$scope', 'configurationService', '$filter', function ($scope, configurationService, $filter) {

        $scope.items = {};

        configurationService.getItems().then(function (response) {
            $scope.items = response.data;
        });

        $scope.configurationChanged = function (key) {
            configurationService.set(key, $scope.items[key].value);
        }

    }]);
