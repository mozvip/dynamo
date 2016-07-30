'use strict';

angular.module('dynamo.magazines', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/magazines/:status', {
    templateUrl: 'magazines/magazines.html',
    controller: 'MagazinesCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }]
    }    
  });
}])

.controller('MagazinesCtrl', ['$scope', '$routeParams', 'downloadableService', 'languages', 'fileListService', 'searchResultsService', '$uibModal', 'filterFilter', function( $scope, $routeParams, downloadableService, languages, fileListService, searchResultsService, $uibModal, filterFilter ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.languages = languages.data;

  $scope.pageContents = [];
  downloadableService.find( 'MAGAZINEISSUE', $routeParams.status ).then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, 24 );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.want = function( downloadable ) {
    downloadableService.want( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.redownload = function( downloadable ) {
    downloadableService.redownload( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * 24;
    $scope.pageContents = $scope.filteredList.slice( start, start + 24);
  }

  $scope.filterChanged = function() {

    var filterObject = {'name': $scope.filter};
    if ($scope.filterLanguage) {
      filterObject.language = $scope.filterLanguage;
    }
    if ($scope.filterYear) {
      filterObject.year = $scope.filterYear;
    }    

    $scope.filteredList = filterFilter($scope.allItems, filterObject);
    $scope.currentPage = 1;
    $scope.pageChanged();
  }

  $scope.openSearchResults = function( downloadable ) {
    searchResultsService.openModal( downloadable );
  }  

  $scope.openFileList = function ( downloadable) {

    var modalInstance = $uibModal.open({
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

    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

}]);
