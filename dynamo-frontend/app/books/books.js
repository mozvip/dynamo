'use strict';

angular.module('dynamo.books', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/books/:status', {
    templateUrl: 'books/books.html',
    controller: 'BooksCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }]
    }    
  }).when('/books-configuration', {
    templateUrl: 'configuration/configuration-template.html',
    controller: 'BooksConfigCtrl'
  });
}])

.controller('BooksCtrl', ['$scope', '$routeParams', 'downloadableService', 'fileListService', 'searchResultsService', '$uibModal', 'filterFilter', 'languages', function( $scope, $routeParams, downloadableService, fileListService, searchResultsService, $uibModal, filterFilter, languages ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.languages = languages.data;

  $scope.pageContents = [];
  downloadableService.find( 'BOOK', $routeParams.status ).then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, 24 );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.want = function( downloadable ) {
    downloadableService.want( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();

    $rootScope.booksSuggestionCount = $scope.allItems.length;
    $rootScope.booksWantedCount ++;
  }

  $scope.redownload = function( downloadable ) {
    downloadableService.redownload( downloadable.id );
    $scope.allItems = filterFilter($scope.allItems, {'id': '!' + downloadable.id });
    $scope.filteredList = filterFilter($scope.filteredList, {'id': '!' + downloadable.id });
    $scope.pageChanged();
  }

  $scope.openSearchResults = function( downloadable ) {
    searchResultsService.openModal( downloadable );
  }

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * 24;
    $scope.pageContents = $scope.filteredList.slice( start, start + 24);
  }

  $scope.filterChanged = function() {
    $scope.filteredList = filterFilter($scope.allItems, {'name': $scope.filter });
    $scope.currentPage = 1;
    $scope.pageChanged();
  }

  $scope.openFileList = function ( downloadable) {

    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

}]);
