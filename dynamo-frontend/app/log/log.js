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


.controller('StackTraceCtrl', ['$scope', '$uibModal', 'stackTrace', 'BackendService', function( $scope, $uibModal, stackTrace, BackendService ) {


  
}])

.controller('WantedCtrl', ['$scope', '$routeParams', 'downloadableService', 'filterFilter', 'BackendService', 'wanted', function( $scope, $routeParams, downloadableService, filterFilter, BackendService, wanted ) {

  $scope.wanted = wanted.data;

  $scope.filterChanged = function() {
    $scope.wanted = filterFilter(wanted.data, {'name' : $scope.filter});
  }

  $scope.imageURL = function( url ) {
    return BackendService.getImageURL( url );
  }  

}])

.controller('StackTraceCtrl', ['$scope', 'stackTrace','$uibModal', 'filterFilter', 'BackendService', function( $scope, stackTrace, $uibModal, filterFilter, BackendService ) {

  $scope.stackTrace = stackTrace.data;

  $scope.stackTrace.forEach(function(element) {
    element.customCode = element.className.startsWith('java.') || element.className.startsWith('sun.');
  }, this);

}])

.controller('LogCtrl', ['$scope', '$routeParams', 'downloadableService', 'log', '$uibModal', 'filterFilter', 'BackendService', function( $scope, $routeParams, downloadableService, log, $uibModal, filterFilter, BackendService ) {

  $scope.allData = log.data;
  $scope.totalItems = $scope.allData.length; 
  $scope.filteredList = [];
  $scope.pageSize = 100;

  $scope.currentPage = 1;

  $scope.openStackTrace = function( logItem ) {
    var modalInstance = $uibModal.open({
      templateUrl: 'log/stack-trace.html',
      controller: 'StackTraceCtrl',
      size: 'lg',
      resolve: {
        stackTrace: function () {
          return BackendService.get('log/stack-trace/' + logItem.id);
        }
      }
    });
  }

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
