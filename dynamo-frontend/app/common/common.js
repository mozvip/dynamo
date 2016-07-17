'use strict'

angular.module('dynamo.common', ['ngRoute', 'ngResource'])

.factory('eventDataService', ['backendHostAndPort', '$websocket', function(backendHostAndPort, $websocket) {
  // Open a WebSocket connection
  var dataStream = $websocket('ws://' + backendHostAndPort + '/websocket/messages');

  dataStream.onMessage(function(message) {
    var event = JSON.parse(message.data);
    if (event.type == 'info') {
      toastr.info( event.body, event.title );
    } else {
      toastr.info( type );
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
      return $http.get('http://' + backendHostAndPort + '/services/configuration');
    },
    'set' : function( key, value ) {
      return $http.post('http://' + backendHostAndPort + '/services/configuration', { 'key' : key, 'value' : value});
    }

  }

  return configurationService;

}])

.factory('fileListService', ['backendHostAndPort', '$http', function(backendHostAndPort, $http){
  var fileListService = {};
  fileListService.get =  function( id ) {
    return $http.get('http://' + backendHostAndPort + '/services/file-list/' + id);
  }
  fileListService.del = function( path ) {
    return $http.delete('http://' + backendHostAndPort + '/services/file-list?path=' + path);
  }
  return fileListService;
}])

. controller('FileListCtrl', ['$scope', '$uibModalInstance', 'fileList', function($scope, $uibModalInstance, fileList) {

  $scope.files = fileList.data;

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

.factory('downloadableService', ['backendHostAndPort', '$http', function(backendHostAndPort, $http){
  var downloadableService = {};
  downloadableService.find = function( type, status ) {
    return $http.get('http://' + backendHostAndPort + '/services/downloadable?type=' + type + '&status=' + status);
  }
  downloadableService.counts = function() {
    return $http.get('http://' + backendHostAndPort + '/services/downloadable/counts');
  }
  downloadableService.want = function( downloadableId ) {
    return $http.post('http://' + backendHostAndPort + '/services/downloadable/want/' + downloadableId);
  }
  downloadableService.redownload = function( downloadableId ) {
    return $http.post('http://' + backendHostAndPort + '/services/downloadable/redownload/' + downloadableId);
  }
  downloadableService.delete = function( downloadableId ) {
    return $http.delete('http://' + backendHostAndPort + '/services/downloadable/' + downloadableId);
  }
  downloadableService.updateImage = function( downloadableId ) {
    return $http.post('http://' + backendHostAndPort + '/services/downloadable/updateImage?id=' + downloadableId);
  }
  return downloadableService;
}])

.directive('ngConfirmClick', [
  function(){
    return {
      link: function (scope, element, attr) {
        var msg = attr.ngConfirmClick || "Are you sure?";
        var clickAction = attr.confirmedClick;
        element.bind('click',function (event) {
          if ( window.confirm(msg) ) {
            scope.$eval(clickAction)
          }
        });
      }
    };
  }])
  ;
