var auth = 'Basic WjhlYWYyam1ZOEY1TG90WnJaZHQxbVlKdUVWUStuN1hyckUzL3RTT05tODpaOGVhZjJqbVk4RjVMb3RaclpkdDFtWUp1RVZRK243WHJyRTMvdFNPTm04';

$('body').bind('touchmove', function(e) {
  e.preventDefault()
});

angular.module('hottrade', ['gajus.swing', 'ui.router'])

.config(function($stateProvider, $urlRouterProvider) {

})

.controller('SwipeCardsCtrl', function($scope, $http) {

  $scope.matches = [];
  $http.get('/api/industry/Technology').success(function(data) {
    $scope.matches = _.first(data, 20);
  });

  $scope.picked = [];

  $scope.swipeLeft = function(index) {
    console.log(index);
    $scope.matches.splice(index, 1);
    $scope.$apply();
  };

  $scope.swipeRight = function(index) {
    console.log(index);
    $scope.picked.push($scope.matches.splice(index, 1)[0]);
    $scope.$apply();
  };

  $scope.nextWindow = function() {
    if ($scope.matches.length > 0) {
      return;
    }
    $('#nextLink').click();
  };

});
