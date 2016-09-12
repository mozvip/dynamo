'use strict';

angular.module('dynamo.tvshows', ['ngRoute', 'ngResource'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/tvshows', {
    templateUrl: 'tvshows/tvshows.html',
    controller: 'TVShowsCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      unrecognizeds: ['tvShowsService', function( tvShowsService ) {
        return tvShowsService.getUnrecognized();
      }]
    }
  }).when('/tvshows-add', {
    templateUrl: 'tvshows/tvshows-add.html',
    controller: 'TVShowsAddCtrl',
    resolve: {
      configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
      }],      
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }]      
    }
  }).when('/tvshows-configuration', {
    templateUrl: 'configuration/configuration-template.html',
    controller: 'TVShowsConfigCtrl',
    resolve: {
      configuration: ['configurationService', function(  configurationService  ) {
        return configurationService.getItems();
      }]
    }
  }).when('/tvshows-wanted', {
    templateUrl: 'tvshows/episodes.html',
    controller: 'EpisodesCtrl'
  }).when('/tvshows-unrecognized', {
    templateUrl: 'tvshows/tvshows-unrecognized.html',
    controller: 'TVShowsUnrecognizedCtrl',
    resolve: {
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      configuration: ['configurationService', function (configurationService) {
          return configurationService.getItems();
      }],      
      unrecognizeds: ['tvShowsService', function( tvShowsService ) {
        return tvShowsService.getUnrecognized();
      }]
    }
  }).when('/tvshow-detail/:tvShowId', {
    templateUrl: 'tvshows/tvshow-detail.html',
    controller: 'TVShowDetailsCtrl',
    resolve: {
      tvshow: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getTVShow( $route.current.params.tvShowId );
      }],
      episodes: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getEpisodes( $route.current.params.tvShowId );
      }],
      languages: ['languageService', function(  languageService  ) {
        return languageService.find();
      }],
      unrecognizedFiles: ['tvShowsService', '$route', function(  tvShowsService, $route  ) {
        return tvShowsService.getUnrecognizedFiles( $route.current.params.tvShowId );
      }]      
    }
  });
}])

.factory('tvdbService', ['BackendService', function(BackendService){
  var tvdbService = {};
  tvdbService.find = function( title ) {
    return BackendService.get('tvdb/search', {'title' : title, 'language' : 'EN'});
  }
  tvdbService.find = function( title, year, language ) {
    var searchData = { 'title' : title, 'language': 'EN' };
    if (year) {
      searchData.year = year;
    }
    if (language) {
      searchData.language = language;
    }
    return BackendService.get('tvdb/search', searchData);
  }
  return tvdbService;
}])

.factory('tvShowsService', ['BackendService', function(BackendService){
  var tvShowsService = {};
  tvShowsService.find = function() {
    return BackendService.get('tvshows');
  }
  tvShowsService.getTVShow = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId);
  }
  tvShowsService.getFolders = function() {
    return BackendService.get('tvshows/folders');
  }
  tvShowsService.getEpisodes = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId + '/episodes');
  }
  tvShowsService.rescan = function( tvshowId ) {
    return BackendService.post('tvshows/rescan/' + tvshowId);
  }
  tvShowsService.delete = function( tvshowId ) {
    return BackendService.delete('tvshows/' + tvshowId);
  }
  tvShowsService.getUnrecognized = function() {
    return BackendService.get('tvshows/unrecognized');
  }
  tvShowsService.associate = function( folder, tvdbId, metadataLanguage, audioLanguage, subtitlesLanguage ) {
    return BackendService.post('tvshows/associate', {'folder': folder, 'tvdbId': tvdbId, 'metadataLanguage': metadataLanguage, 'audioLanguage': audioLanguage, 'subtitlesLanguage': subtitlesLanguage});
  }
  tvShowsService.save = function( folder, tvdbId, metadataLanguage, audioLanguage, subtitlesLanguage ) {
    return BackendService.post('tvshows/save', {'folder': folder, 'tvdbId': tvdbId, 'metadataLanguage': metadataLanguage, 'audioLanguage': audioLanguage, 'subtitlesLanguage': subtitlesLanguage});
  }
  tvShowsService.getUnrecognizedFiles = function( tvshowId ) {
    return BackendService.get('tvshows/' + tvshowId + '/unrecognized');
  }
  return tvShowsService;
}])

.controller('TVShowDetailsCtrl', ['$scope', 'tvshow', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'languages', 'episodes', 'unrecognizedFiles', 'BackendService', '$location', function( $scope, tvshow, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal, filterFilter, languages, episodes, unrecognizedFiles, BackendService, $location ) {

  $scope.tvshow = tvshow.data;
  $scope.episodes = episodes.data;
  $scope.languages = languages.data;
  $scope.unrecognizedFiles = unrecognizedFiles.data;

  $scope.imageURL = function( url ) {
    return BackendService.getImageURL( url );
  }

  $scope.availableEpisodes = [];

  $scope.episodes.forEach(function(episode) {
    if (episode.status == 'IGNORED' || episode.status == 'WANTED' || episode.status == 'SNATCHED') {
      $scope.availableEpisodes.push( episode );
    }    
  }, this);

  $scope.saveTVShow = function() {
    BackendService.post('tvshows/save', $scope.tvshow );
  }

  $scope.selectedEpisode = {};

  $scope.wrongShow = function() {
    return $uibModal.open({
      animation: false,
      templateUrl: 'tvShowSearch.html',
      controller: 'TVShowsSearchCtrl',
      size: 'lg',
      resolve: {
        languages: function () {
          return $scope.languages;
        },
        tvshow: function () {
          return $scope.tvshow;
        }
      }
    }); 
  }

  $scope.assignEpisode = function( file ) {
    downloadableService.assign( file.id, $scope.selectedEpisode[file.id].id ).then(
      function( response ) {
        $scope.selectedEpisode[file.id].status = 'DOWNLOADED';
        $scope.availableEpisodes = filterFilter($scope.availableEpisodes, {'id': '!' + $scope.selectedEpisode[file.id].id });
        $scope.unrecognizedFiles = filterFilter($scope.unrecognizedFiles, {'id': '!' + file.id });
      }
    );
  }

  $scope.want = function( episode ) {
    downloadableService.want( episode.id ).then( function( response ) {
      episode.status = 'WANTED';
    } );
  }

  $scope.redownload = function( episode ) {
    downloadableService.redownload( episode.id ).then( function( response ) {
      episode.status = 'WANTED';
      episode.label = '';
    });
  }

  $scope.rescan = function() {
    tvShowsService.rescan( $scope.tvshow.id );
  }

  $scope.openFileList = function ( downloadable) {
    var modalInstance = fileListService.openModal( downloadable );
    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    });
  };

  $scope.deleteTVShow = function() {
    tvShowsService.delete( $scope.tvshow.id ).then( function() {
      $location.path("/tvshows");
    });
  }

  $scope.wantSeason = function( seasonNumber ) {
    $scope.episodes.forEach(function( episode ) {
      if (episode.seasonNumber == seasonNumber && episode.status != 'DOWNLOADED' && episode.status != 'FUTURE') {
        $scope.want( episode );
      }
    }, this);
  }

  $scope.deleteFile = function( file ) {
    BackendService.delete('tvshows/unrecognized/' + file.id).then( function( response ) {
      $scope.unrecognizedFiles = filterFilter($scope.unrecognizedFiles, {'id': '!' + file.id });
    });
  }

  $scope.delete = function( episode ) {
    downloadableService.delete( episode.id ).then( function() {
      episode.status = 'IGNORED';
    });
  }

  $scope.deleteSeason = function( seasonNumber ) {
    $scope.episodes.forEach(function( episode ) {
      if (episode.seasonNumber == seasonNumber && episode.status != 'FUTURE') {
        $scope.delete( episode );
      }
    }, this);
  }

}])

.controller('TVShowsSearchCtrl', ['$scope', 'BackendService', 'tvShowsService', 'tvdbService', 'languages', 'tvshow', function( $scope, BackendService, tvShowsService, tvdbService, languages, tvshow ) {

  $scope.tvshow = tvshow;
  $scope.languages= languages;

  $scope.name = tvshow.name;
  $scope.year = tvshow.year;

  $scope.searchTVShow = function() {
    tvdbService.find( $scope.name, $scope.year, $scope.searchLanguage ).then( function( response ) {
      $scope.results = response.data;
    });
  }  
  
  $scope.selectTVShow = function( tvshow ) {
    alert(tvshow.seriesName);
  }


}])

.controller('TVShowsUnrecognizedCtrl', ['$scope', 'BackendService', 'tvShowsService', 'tvdbService', 'configuration', 'languages', 'unrecognizeds', '$location', function( $scope, BackendService, tvShowsService, tvdbService, configuration, languages, unrecognizeds, $location ) {

  $scope.unrecognizeds = unrecognizeds.data;
  $scope.languages = languages.data;

  $scope.searchLanguage = configuration.data['TVShowManager.metaDataLanguage'].value;

  $scope.searchText = undefined;

  $scope.selectFolder = function( folder ) {
    $scope.selectedFolder = folder;

    var folderElements = folder.path.split( new RegExp(/[\/\\]/));

    $scope.searchText = folderElements[folderElements.length - 1];
  }

  $scope.cancelSelection = function() {
    $scope.selectedFolder = undefined;
    $scope.results = undefined;
  }

  $scope.searchTVShow = function() {
    tvdbService.find( $scope.searchText, $scope.year, $scope.searchLanguage ).then( function( response ) {
      $scope.results = response.data;
    });
  }

  $scope.selectTVShow = function( tvshow ) {
    tvShowsService.associate( $scope.selectedFolder.path, tvshow.id, $scope.searchLanguage, tvshow.language.toUpperCase(), $scope.subtitlesLanguage ).then(
      function( response ) {
        $location.path("/tvshow-detail/" + response.data);
      }
    );    
  }

}])

.controller('TVShowsAddCtrl', ['$scope', '$location', 'tvShowsService', 'tvdbService', 'BackendService', 'languages', 'configuration', function( $scope, $location, tvShowsService, tvdbService, BackendService, languages, configuration ) {

  $scope.languages = languages.data;

  $scope.language = configuration.data['TVShowManager.metaDataLanguage'].value;

  $scope.results = [];
  $scope.searchTVShow = function() {
    tvdbService.find( $scope.title, $scope.year, $scope.language ).then( function( response ) {
      $scope.results = response.data;
    });
  }

  $scope.audioLanguage = configuration.data['TVShowManager.audioLanguage'].value;
  $scope.subtitlesLanguage = configuration.data['TVShowManager.subtitlesLanguage'].value;
  $scope.selectedTVShow = undefined;

  $scope.selectTVShow = function( tvshow ) {
    $scope.audioLanguage = tvshow.language.toUpperCase();
    $scope.selectedTVShow = tvshow;
  }

  $scope.cancelSelection = function() {
    $scope.selectedTVShow = undefined;
  }

  $scope.addTVShow = function( id ) {
    BackendService.post("tvshows/add", {"tvdbId": id, 'seriesName': $scope.selectedTVShow.seriesName, 'audioLanguage': $scope.audioLanguage, 'subtitlesLanguage': $scope.subtitlesLanguage}).then(
      function( response ) {
        $location.path("/tvshow-detail/" + response.data);
      }, 
      function( reponse ) {
        alert(response.data);
      }
    );
  }

}])

.controller('TVShowsConfigCtrl', ['$scope', '$rootScope', 'configurationService', 'configuration', function( $scope, $rootScope, configurationService, configuration ) {

  $scope.config = configuration.data;

  $scope.itemsToConfigure = [
    $scope.config['TVShowManager.folders'],
    $scope.config['TVShowManager.seasonFolderPattern'],
    $scope.config['TVShowManager.audioLanguage'],
    $scope.config['TVShowManager.metaDataLanguage'],
    $scope.config['TVShowManager.subtitlesLanguage'],
    $scope.config['TVShowManager.tvshowEpisodeProviders'],
    $scope.config['TVShowManager.tvShowSeasonProviders'],
    $scope.config['TVShowManager.wordsBlackList'],
    $scope.config['TVShowManager.tvShowQualities']
  ];

    $scope.saveSettings = function () {
      configurationService.saveItems( $scope.itemsToConfigure );
    }

}])

.controller('TVShowsCtrl', ['$scope', '$routeParams', 'tvShowsService', 'tvdbService', 'downloadableService', 'fileListService', '$uibModal', 'filterFilter', 'BackendService', 'languages', 'unrecognizeds', function( $scope, $routeParams, tvShowsService, tvdbService, downloadableService, fileListService, $uibModal,  filterFilter, BackendService, languages, unrecognizeds ) {

  $scope.currentPage = 1;
  $scope.allItems = [];
  $scope.filteredList = [];

  $scope.unrecognizeds = unrecognizeds.data;

  $scope.languages = languages.data;

  $scope.imageURL = function( url ) {
    return BackendService.getImageURL( url );
  }  

  $scope.pageContents = [];
  tvShowsService.find().then( function( response ) {
    $scope.allItems = response.data;
    $scope.pageContents = $scope.allItems.slice( 0, 24 );
    $scope.filteredList = $scope.allItems.slice( 0 );
  });

  $scope.pageChanged = function() {
    var start = ($scope.currentPage - 1) * 24;
    $scope.pageContents = $scope.filteredList.slice( start, start + 24);
  }

  $scope.filterChanged = function() {
    $scope.filteredList = filterFilter($scope.allItems, {'name': $scope.filter });
    $scope.currentPage = 1;
    $scope.pageChanged();
  }

  $scope.toggleAutoDownload = function( tvshow ) {
    BackendService.post('tvshows/toggleAutoDownload/' + tvshow.id).then( function(response) {
      tvshow.autoDownload = !tvshow.autoDownload; 
    });
  }

}]);
