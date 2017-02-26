'use strict';

angular.module('dynamo.music', ['ngRoute', 'ngResource'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/music/:status', {
      templateUrl: 'music/music.html',
      controller: 'MusicCtrl'
    }).when('/music-configuration', {
      templateUrl: 'configuration/configuration-template.html',
      controller: 'MusicConfigCtrl',
      resolve: {
        configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
        }]
      }
    }).when('/music-album/:albumId', {
      templateUrl: 'music/music-album.html',
      controller: 'MusicAlbumCtrl',
      resolve: {
        album: ['BackendService', '$route', function (BackendService, $route) {
          return BackendService.get('music/album/' + $route.current.params.albumId);
        }],
        files: ['fileListService', '$route', function (fileListService, $route) {
          return fileListService.get( $route.current.params.albumId);
        }]
      }
    });
  }])

  .controller('MusicAlbumCtrl', ['$scope', 'downloadableService', 'BackendService', 'album', 'files', '$location', function ($scope, downloadableService, BackendService, album, files, $location) {

    $scope.album = album.data;
    $scope.files = files.data;

    $scope.saveData = function() {
      BackendService.post('music/save', $scope.album).then( function( response ) {
        $location.path('music-album/' + response.data);
      });
    }

    $scope.filesMisplaced = false;

    $scope.files.forEach(function(file) {
      if (file.filePath.startsWith($scope.album.folder)) {
        file.wrongFolder = false;
        file.fileName = file.filePath.substr($scope.album.folder.length+1);
      } else {
        file.fileName = file.filePath;
        file.wrongFolder = true;
        $scope.filesMisplaced = true;
      }
    }, this);

    $scope.imageURL = function( url ) {
      return BackendService.getImageURL( url );
    }

    $scope.updateImage = function () {
      downloadableService.updateImage($scope.album.id);
    }

    $scope.redownload = function() {
      BackendService.post('downloadable/redownload/' + $scope.album.id).then( function( response ) {
        $location.path('music/DOWNLOADED');
      });
    }

    $scope.fixFolder = function() {
      $scope.files.forEach(function(file) {
        if (file.wrongFolder) {
          BackendService.post('file-list/moveToFolder?downloadableId=' + $scope.album.id + '&file=' + file.filePath + '&toFolder=' + $scope.album.folder).then( function( response ) {
            file.wrongFolder = false;
            file.filePath = response.data;
          });
        }
      }, this);
    }

    $scope.selectAll = function() {
      $scope.files.forEach(function(file) {
        file.selected = true;
      }, this);
    }


  }])

  .controller('MusicCtrl', ['$scope', '$rootScope', '$routeParams', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'BackendService', function ($scope, $rootScope, $routeParams, downloadableService, fileListService, $uibModal, filterFilter, BackendService) {

    $scope.currentPage = 1;
    $scope.allItems = [];
    $scope.filteredList = [];

    $scope.itemsPerPage = 18;

    $scope.imageURL = function (url) {
      return BackendService.getImageURL(url);
    }

    $scope.pageContents = [];
    downloadableService.find('MUSICALBUM', $routeParams.status).then(function (response) {
      $scope.allItems = response.data;
      $scope.pageContents = $scope.allItems.slice(0, $scope.itemsPerPage);
      $scope.filteredList = $scope.allItems.slice(0);
    });

    $scope.want = function (downloadable) {
      downloadableService.want(downloadable.id);
      $scope.removeFromList( downloadable );

      $rootScope.musicAlbumsSuggestionCount = $scope.allItems.length;
      $rootScope.musicAlbumsWantedCount++;
    }

    $scope.removeFromList = function (downloadable) {
      $scope.allItems = filterFilter($scope.allItems, { 'id': '!' + downloadable.id });
      $scope.filteredList = filterFilter($scope.filteredList, { 'id': '!' + downloadable.id });
      $scope.pageChanged();
    }

    $scope.delete = function (downloadable) {
      downloadableService.delete(downloadable.id);
      $scope.removeFromList(downloadable);
    }

    $scope.redownload = function (downloadable) {
      downloadableService.redownload(downloadable.id);
      downloadable.status = 'WANTED';
    }

    $scope.pageChanged = function () {
      var start = ($scope.currentPage - 1) * $scope.itemsPerPage;
      $scope.pageContents = $scope.filteredList.slice(start, start + $scope.itemsPerPage);
    }

    $scope.filterChanged = function () {
      var filterObject = {};
      if ($scope.filterName) {
        filterObject['name'] = $scope.filterName;
      }
      if ($scope.filterArtist) {
        filterObject['artistName'] = $scope.filterArtist;
      }
      $scope.filteredList = filterFilter($scope.allItems, filterObject);
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

      $scope.openFileList = function ( downloadable) {
    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

  }])

  .controller('MusicConfigCtrl', ['$scope', '$rootScope', 'configurationService', 'configuration', function ($scope, $rootScope, configurationService, configuration) {

    $scope.config = configuration.data;

    $scope.itemsToConfigure = [
      $scope.config['MusicManager.folders'],
      $scope.config['MusicManager.musicDownloadProviders'],
      $scope.config['MusicManager.musicQuality'],
      $scope.config['MusicManager.suggesters']
    ];

    $scope.saveSettings = function () {
      configurationService.saveItems($scope.itemsToConfigure);
    }

  }]);
