'use strict'

angular.module('dynamo.common', ['ngRoute', 'ngResource'])

.factory('eventDataService', ['BackendService', function( BackendService ) {
  // Open a WebSocket connection
  var dataStream = BackendService.getWebSocket('messages');

  dataStream.onMessage(function(message) {
    var event = JSON.parse(message.data);
    if (event.type == 'info') {
      toastr.info( event.body, event.title );
    } else if (event.type == 'error') {
      toastr.error( event.body, event.title );
    } else if (event.type == 'success') {
      toastr.success( event.body, event.title );
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

.factory('configurationService', ['BackendService', '$http', '$filter', function(BackendService, $http, $filter){

  var configurationService = {

    'get' : function( key ) {
      return BackendService.get('configuration/' + key);
    },
    'getItems' : function() {
      return BackendService.get('configuration/items');
    },
    'saveItems': function( items ) {
      var itemsToSave = {};
      items.forEach(function(element) {
        itemsToSave[element.key] = element.value;
      }, this);
      return BackendService.post('configuration', itemsToSave);
    }
  }

  return configurationService;

}])

.factory('searchResultsService', ['BackendService', '$http', '$uibModal', function(BackendService, $http, $uibModal) {

  var searchResultsService = {};

  searchResultsService.get =  function( id ) {
    return BackendService.get('searchResults/' + id);
  }  

  searchResultsService.openModal = function( downloadable) {

    $uibModal.open({
        animation: false,
        templateUrl: 'common/searchResults.html',
        controller: 'SearchResultsCtrl',
        size: 'lg',
        resolve: {
          searchResultsList: function () {
            return searchResultsService.get( downloadable.id );
          },
          downloadable: function () {
            return downloadable;
          }
        }
      });
    }

    return searchResultsService;

}])

.factory('fileListService', ['BackendService', '$http', '$uibModal', function(BackendService, $http, $uibModal){
  var fileListService = {};
  fileListService.get =  function( id ) {
    return BackendService.get('file-list/' + id);
  }
  fileListService.delete = function( path ) {
    return BackendService.delete('file-list?path=' + path);
  }
  fileListService.downloadURL = function( path ) {
    return BackendService.getBackendURL() + 'file-list/download?path=' + path;
  }
  fileListService.openModal = function( downloadable ) {
    return $uibModal.open({
      animation: false,
      templateUrl: 'common/fileList.html',
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

.controller('FileListCtrl', ['$scope', '$uibModalInstance', 'filterFilter', 'fileList', 'fileListService', function($scope, $uibModalInstance, filterFilter, fileList, fileListService) {

  $scope.files = fileList.data;

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

  $scope.downloadURL = function( file ) {
    return fileListService.downloadURL( file );
  }

  $scope.delete = function( file ) {
    fileListService.delete( file.filePath ).then( function() {
      $scope.files = filterFilter( $scope.files, {'filePath' : '!' + file.filePath}) ;
      if ($scope.files.length == 0) {
        $uibModalInstance.dismiss('cancel');
      }
    });
  }

}])

.controller('SearchResultsCtrl', ['$scope', '$uibModalInstance', 'downloadable', 'searchResultsList', 'BackendService', function($scope, $uibModalInstance, downloadable, searchResultsList, BackendService) {

  $scope.searchResults = searchResultsList.data;
  $scope.downloadable = downloadable;

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

  $scope.forceNewSearch = function() {
    BackendService.post( 'downloadable/force-search/' + $scope.downloadable.id + '?reset=true' );
    $scope.downloadable.status = 'WANTED';
    $uibModalInstance.close();
  };

}])

.factory('languageService', ['BackendService', function(BackendService){
  var languageService = {};
  languageService.find = function( type, status ) {
    return BackendService.getAndCache('languages');
  }
  return languageService;
}])

.directive('configurationItem', function() {
  return {
    restrict: 'E',
    scope: {
      item: '='
    },
    templateUrl: 'common/configuration-item.html',
    link: function(scope, element, attr) {

      if (scope.item.type == 'boolean') {
        scope.item.value = ( scope.item.value === 'true' );
      }

      if (scope.item.type == 'int') {
        scope.item.value = parseInt(scope.item.value);
      }

      if (scope.item.type == 'float') {
        scope.item.value = parseFloat(scope.item.value);
      }

      if (scope.item.list) {

        scope.refreshRemainingValues = function() {
          var remainingProperties = Object.keys( scope.item.allowedValues ).filter( function(value) {
            return !scope.item.values.find( function(item) {
              return item.value == value;
          })});
          scope.item.remainingValues = {};
          remainingProperties.forEach(function(property) {
            scope.item.remainingValues[property] = scope.item.allowedValues[property];
            scope.item.newValue = property;
          }, this);
        }

        var values = scope.item.value ? scope.item.value.split(';') : [];
        if (values[values.length-1] == '') {
          values.splice( values.length - 1, 1);
        }
        scope.item.values = values.map( function( element ) {
          return {'value': element};
        });
        if (scope.item.allowedValues) {
          scope.refreshRemainingValues();
        }
        scope.hasRemainingValues = function() {
          if (scope.item.remainingValues && Object.keys(scope.item.remainingValues).length == 0) {
            return false;
          }
          return true;
        }

        scope.recalculateValue = function() {
          var itemValues = scope.item.values.map( function( element ) {
            return element.value;
          });          
          scope.item.value = itemValues.reduceRight( function(previousValue, currentValue, currentIndex, array) {
            return currentValue + ';' + previousValue;
          } );
        }
  
        scope.changeValue = function() {
          if (scope.item.remainingValues) {
            scope.refreshRemainingValues();
          }
          scope.recalculateValue();
        }

        scope.removeRow = function( index ) {
          scope.item.values.splice( index, 1 );
          scope.changeValue();
        }

        scope.addRow = function() {
          var newItem = {};
          if (scope.item.newValue) {
            newItem['value'] = scope.item.newValue;
          }
          scope.item.values.push( newItem );
          scope.changeValue();
        }
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

.factory('downloadableService', ['BackendService', '$http', function(BackendService, $http){
  var downloadableService = {};
  downloadableService.find = function( type, status ) {
    return BackendService.get('downloadable?type=' + type + '&status=' + status);
  }
  downloadableService.counts = function() {
    return BackendService.get('downloadable/counts');
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
    return BackendService.delete('downloadable/' + downloadableId);
  }
  downloadableService.updateImage = function( downloadableId ) {
    return BackendService.post('downloadable/update-cover-image/' + downloadableId);
  }

  return downloadableService;
}]);
