var auth = 'Basic WjhlYWYyam1ZOEY1TG90WnJaZHQxbVlKdUVWUStuN1hyckUzL3RTT05tODpaOGVhZjJqbVk4RjVMb3RaclpkdDFtWUp1RVZRK243WHJyRTMvdFNPTm04';

window.disableScroll = true;
$('body').bind('touchmove', function(e) {
  if (window.disableScroll) {
    e.preventDefault();
  }
});

angular.module('hottrade', ['gajus.swing', 'ui.router'])

.config(function($stateProvider, $urlRouterProvider) {

})

.controller('MainCtrl', function($scope, $http) {

  $scope.sectors = ["Consumer, Cyclical", "Financial", "Consumer, Non-cyclical", "Energy", "Technology", "Industrial", "Utilities", "Communications", "Basic Materials", "Diversified"];
  $scope.survey = {
    sector: $scope.sectors[0],
    risk: 'safe'
  };

  $scope.matches = [];

  $scope.matchStocks = function() {
    $http.get('/api/industry/' + $scope.survey.sector).success(function(data) {
      var matches = _.first(data, 20);
      async.eachLimit(matches, 5, function(match, done) {
        var str = encodeURIComponent(match.name + ' Logo');
        var link = 'https://api.datamarket.azure.com/Data.ashx/Bing/Search/Image?$format=json&Query=%27' + str + '%27';

        $http.get(link, {
          headers: {
            Authorization: auth
          }
        }).success(function(data) {
          match.img = data.d.results[0].MediaUrl;
          done();
        });

      }, function() {
        console.log('done loading images');
        setTimeout(function() {
          $scope.matches = matches;
          $scope.$apply();
          $('#workLink').click();
        }, 50);
      });
    });
  };


  $scope.kimonoInfo = [];
  $http.jsonp('https://www.kimonolabs.com/api/27zrivbo?apikey=sNaDL4glHt0HIxcbjOTvflpT3ppp85WI&callback=JSON_CALLBACK').success(function(data) {
    $scope.kimonoInfo = data;
    console.log(data);
  });

  $scope.passed = [];
  $scope.picked = [];

  $scope.swipeLeft = function(index) {
    console.log(index);
    $scope.passed.push($scope.matches.splice(index, 1)[0]);
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
    window.disableScroll = false;
  };

});
