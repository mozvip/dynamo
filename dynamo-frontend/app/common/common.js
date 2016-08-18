'use strict'

angular.module('dynamo.common', ['ngRoute', 'ngResource'])

.factory('eventDataService', ['backendHostAndPort', '$websocket', function(backendHostAndPort, $websocket) {
  // Open a WebSocket connection
  var dataStream = $websocket('ws://' + backendHostAndPort + '/websocket/messages');

  dataStream.onMessage(function(message) {
    var event = JSON.parse(message.data);
    if (event.type == 'info') {
      toastr.info( event.body, event.title );
    } else if (event.type == 'error') {
      toastr.error( event.body, event.title );
    } else {
      toastr.info( event.type );
      toastr.info( event.body, event.title );
    }
  });

  var methods = {
    get: function() {
      dataStream.send(JSON.stringify({ action: 'get' }));
    }
  };

  return methods;
}])

.factory('configurationService', ['backendHostAndPort', '$http', '$filter', function(backendHostAndPort, $http, $filter){

  var configurationService = {

    'get' : function( key ) {
      return $http.get('http://' + backendHostAndPort + '/services/configuration/' + key);
    },
    'getItems' : function() {
      return $http.get('http://' + backendHostAndPort + '/services/configuration/items');
    },
    'set' : function( key, value ) {
      return $http.post('http://' + backendHostAndPort + '/services/configuration', { 'key' : key, 'value' : value});
    },
    'setFolders' : function( key, value ) {

      var submittedValue = '';
      value.forEach(function(folder) {
        submittedValue += folder.path;
        submittedValue += ';';
      }, this);
      return $http.post('http://' + backendHostAndPort + '/services/configuration', { 'key' : key, 'value' : submittedValue});
    }

  }

  return configurationService;

}])

.factory('searchResultsService', ['backendHostAndPort', '$http', '$uibModal', function(backendHostAndPort, $http, $uibModal) {

  var searchResultsService = {};

  searchResultsService.get =  function( id ) {
    return $http.get('http://' + backendHostAndPort + '/services/searchResults/' + id);
  }  

  searchResultsService.openModal = function( downloadable) {

    $uibModal.open({
        animation: false,
        templateUrl: 'searchResults.html',
        controller: 'SearchResultsCtrl',
        size: 'lg',
        resolve: {
          searchResultsList: function () {
            return searchResultsService.get( downloadable.id );
          }
        }
      });
    }

    return searchResultsService;

}])

.factory('fileListService', ['backendHostAndPort', '$http', '$uibModal', function(backendHostAndPort, $http, $uibModal){
  var fileListService = {};
  fileListService.get =  function( id ) {
    return $http.get('http://' + backendHostAndPort + '/services/file-list/' + id);
  }
  fileListService.del = function( path ) {
    return $http.delete('http://' + backendHostAndPort + '/services/file-list?path=' + path);
  }
  fileListService.openModal = function( downloadable ) {
    return $uibModal.open({
      animation: false,
      templateUrl: 'fileList.html',
      controller: 'FileListCtrl',
      size: 'lg',
      resolve: {
        fileList: function () {
          return fileListService.get( downloadable.id );
        }
      }
    });    
  }
  return fileListService;
}])

. controller('FileListCtrl', ['$scope', '$uibModalInstance', 'fileList', function($scope, $uibModalInstance, fileList) {

  $scope.files = fileList.data;

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

}])

. controller('SearchResultsCtrl', ['$scope', '$uibModalInstance', 'searchResultsList', function($scope, $uibModalInstance, searchResultsList) {

  $scope.searchResults = searchResultsList.data;

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

}])

.factory('languageService', ['backendHostAndPort', '$http', function(backendHostAndPort, $http){
  var languageService = {};
  languageService.find = function( type, status ) {
    return $http.get('http://' + backendHostAndPort + '/services/languages');
  }
  return languageService;
}])

.directive('configurationList', function() {
  return {
    restrict: 'E',
    scope: {
      item: '='
    },
    templateUrl: 'common/configuration-list.html',
    link: function(scope, element, attr) {
      scope.removeRow = function( index ) {
        scope.item.values.splice( index, 1 );
      }
      scope.addRow = function() {
        scope.item.values.push({});
      }
    }
  }
})

.directive('validFolder', function($q, BackendService) {
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl) {

      ctrl.$asyncValidators.validFolder = function(modelValue, viewValue) {
        if (ctrl.$isEmpty(modelValue)) {
          // consider empty model valid
          return $q.when();
        }
        return BackendService.post('configuration/validFolder', modelValue);
      };
    }
  };
})

.factory('downloadableService', ['BackendService', 'backendHostAndPort', '$http', function(BackendService, backendHostAndPort, $http){
  var downloadableService = {};
  downloadableService.find = function( type, status ) {
    return $http.get('http://' + backendHostAndPort + '/services/downloadable?type=' + type + '&status=' + status);
  }
  downloadableService.counts = function() {
    return $http.get('http://' + backendHostAndPort + '/services/downloadable/counts');
  }
  downloadableService.want = function( downloadableId ) {
    return BackendService.post('downloadable/want/' + downloadableId);
  }
  downloadableService.assign = function( fileId, downloadableId ) {
    return BackendService.post('downloadable/assign/' + fileId + '/' + downloadableId);
  }
  downloadableService.redownload = function( downloadableId ) {
    return BackendService.post('downloadable/redownload/' + downloadableId);
  }
  downloadableService.delete = function( downloadableId ) {
    return $http.delete('http://' + backendHostAndPort + '/services/downloadable/' + downloadableId);
  }
  downloadableService.updateImage = function( downloadableId ) {
    return BackendService.post('downloadable/updateImage?id=' + downloadableId);
  }

  return downloadableService;
}]);
