'use strict';

angular.module('dynamo.configuration', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/configuration', {
    templateUrl: 'configuration/configuration.html',
    controller: 'ConfigurationCtrl'
  });
}])

.directive('dynamicRequired', ['$compile', '$parse', function ($compile, $parse) {
    return {
        restrict: 'A',
        terminal: true,
        priority: 100000,
        link: function (scope, elem) {
            var name = $parse(elem.attr('dynamic-required'))(scope);
            elem.removeAttr('dynamic-required');
            elem.attr('required', name);
            $compile(elem)(scope);
        }
    };
}])

.directive('dynamicIf', ['$compile', '$parse', function ($compile, $parse) {
    return {
        restrict: 'A',
        terminal: true,
        priority: 100000,
        link: function (scope, elem) {
            var name = $parse(elem.attr('dynamic-if'))(scope);
            elem.removeAttr('dynamic-if');
            elem.attr('ng-if', name);
            $compile(elem)(scope);
        }
    };
}])

.controller('ConfigurationCtrl', ['$scope', 'configurationService', '$filter', function($scope, configurationService, $filter) {

  $scope.items = {};

  configurationService.getItems().then( function( response ) {
    $scope.items = response.data;
  });

  $scope.configurationChanged = function( key ) {
    configurationService.set(key, $scope.items[key].value);
  }

}]);
