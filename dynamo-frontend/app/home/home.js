'use strict';

angular.module('dynamo.home', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/welcome', {
    templateUrl: 'home/home.html',
    controller: 'HomeCtrl',
    resolve: {
      disks: ['BackendService', function(  BackendService  ) {
        return BackendService.get('disks');
      }]
    }
  });
}])

.controller('HomeCtrl', ['$scope', '$location', 'disks', function( $scope, $location, disks ) {

    $scope.disks = disks.data;

    $scope.disks.forEach(function(disk) {
      disk.occupation = (disk.totalSpace-disk.freeSpace) / disk.totalSpace * 100;
    }, this);

}]);
