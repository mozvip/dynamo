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

  $scope.allData = log.data;
  $scope.totalItems = $scope.allData.length; 
  $scope.filteredList = [];
  $scope.pageSize = 100;

  $scope.currentPage = 1;

  $scope.filterChanged = function() {

    if ($scope.filterSeverity || $scope.filterMessage) {
      var filterObject = {}; 
      if ($scope.filterSeverity) {
        filterObject['severity'] = $scope.filterSeverity;
      }
      if ($scope.filterTask) {
        filterObject['taskName'] = $scope.filterTask;
      }
      if ($scope.filterMessage) {
        filterObject['message'] = $scope.filterMessage;
      }
      $scope.filteredList = filterFilter($scope.allData, filterObject);
    } else {
      $scope.filteredList = $scope.allData;
    }

    $scope.currentPage = 1;
    $scope.pageChanged();
  }

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * $scope.pageSize;
    $scope.log = $scope.filteredList.slice( start, start + 100 );    
  }

  $scope.filterChanged();
  $scope.pageChanged();

}]);
