'use strict';

angular.module('dynamo.log', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/log', {
    templateUrl: 'log/log.html',
    controller: 'LogCtrl',
    resolve: {
      log: ['BackendService', function(  BackendService  ) {
        return BackendService.get('log');
      }]
    }   
  });
}])

.controller('LogCtrl', ['$scope', '$routeParams', 'downloadableService', 'log', '$uibModal', 'filterFilter', function( $scope, $routeParams, downloadableService, log, $uibModal, filterFilter ) {

  $scope.log = log.data;

}]);
