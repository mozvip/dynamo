'use strict';

angular.module('dynamo.books', ['ngRoute', 'ngResource'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/books/:status', {
      templateUrl: 'books/books.html',
      controller: 'BooksCtrl',
      resolve: {
        languages: ['languageService', function (languageService) {
          return languageService.find();
        }]
      }
    }).when('/books-configuration', {
      templateUrl: 'configuration/configuration-template.html',
      controller: 'BooksConfigCtrl',
      resolve: {
        configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
        }]
      }

    });
  }])

  .controller('BooksConfigCtrl', ['$scope', 'configurationService', 'configuration', function ($scope, configurationService, configuration) {

    $scope.config = configuration.data;

    $scope.itemsToConfigure = [
      $scope.config['BookManager.folders'],
      $scope.config['BookManager.defaultLanguage'],
      $scope.config['BookManager.blackList'],
      $scope.config['BookManager.providers'],
      $scope.config['RefreshBookSuggestionsExecutor.suggesters']
    ];

    $scope.saveSettings = function () {
      configurationService.saveItems($scope.itemsToConfigure);
    }

  }])

  .controller('BooksCtrl', ['$scope', '$routeParams', 'downloadableService', 'fileListService', 'searchResultsService', '$uibModal', 'filterFilter', 'languages', 'BackendService', function ($scope, $routeParams, downloadableService, fileListService, searchResultsService, $uibModal, filterFilter, languages, BackendService) {

    $scope.currentPage = 1;
    $scope.allItems = [];
    $scope.filteredList = [];
    $scope.itemsPerPage = 24;

    $scope.languages = languages.data;

    $scope.imageURL = function (url) {
      return BackendService.getImageURL(url);
    }

    $scope.pageContents = [];
    downloadableService.find('BOOK', $routeParams.status).then(function (response) {
      $scope.allItems = response.data;
      $scope.pageContents = $scope.allItems.slice(0, $scope.itemsPerPage);
      $scope.filteredList = $scope.allItems.slice(0);
    });

    $scope.want = function (downloadable) {
      downloadableService.want(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();

      $rootScope.booksSuggestionCount = $scope.allItems.length;
      $rootScope.booksWantedCount++;
    }

    $scope.redownload = function (downloadable) {
      downloadableService.redownload(downloadable.id);
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.openSearchResults = function (downloadable) {
      searchResultsService.openModal(downloadable);
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * $scope.itemsPerPage;
      $scope.pageContents = $scope.filteredList.slice(start, start + $scope.itemsPerPage);
    }

    $scope.filterChanged = function () {
      $scope.filteredList = filterFilter($scope.allItems, { 'name': $scope.filter });
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.openFileList = function (downloadable) {

      var modalInstance = fileListService.openModal(downloadable);
      modalInstance.result.then(function (selectedItem) {
        $scope.selected = selectedItem;
      });
    };

  }]);
