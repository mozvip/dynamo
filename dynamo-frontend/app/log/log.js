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
  }).when('/wanted', {
    templateUrl: 'log/wanted.html',
    controller: 'WantedCtrl',
    resolve: {
      wanted: ['BackendService', function(  BackendService  ) {
        return BackendService.get('downloadable/wanted');
      }]
    }   
  }).when('/history', {
    templateUrl: 'log/history.html',
    controller: 'HistoryCtrl',
    resolve: {
      history: ['BackendService', function(  BackendService  ) {
        return BackendService.get('history');
      }]
    }   
  });
}])

.controller('WantedCtrl', ['$scope', '$routeParams', 'downloadableService', 'filterFilter', 'wanted', function( $scope, $routeParams, downloadableService, $uibModal, filterFilter, wanted ) {

  $scope.allData = wanted.data;
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

}])

.controller('HistoryCtrl', ['$scope', '$routeParams', 'downloadableService', 'history', '$uibModal', 'filterFilter', function( $scope, $routeParams, downloadableService, history, $uibModal, filterFilter ) {

  $scope.allData = history.data;
  $scope.totalItems = $scope.allData.length; 
  $scope.filteredList = [];
  $scope.pageSize = 100;

  $scope.currentPage = 1;

  $scope.filterChanged = function() {

    if ($scope.filterStatus || $scope.filterComment) {
      var filterObject = {}; 
      if ($scope.filterStatus) {
        filterObject['status'] = $scope.filterStatus;
      }
      if ($scope.filterComment) {
        filterObject['comment'] = $scope.filterComment;
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
    $scope.history = $scope.filteredList.slice( start, start + 100 );    
  }

  $scope.filterChanged();
  $scope.pageChanged();

}]);
