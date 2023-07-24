import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  TouchableOpacity,
  TouchableHighlight,
  Platform,
  NativeModules,
  Linking,
  ScrollView,
  AsyncStorage
} from 'react-native';
import AFGAnimatedCircularProgress from '../components/AFGAnimatedCircularProgress';
import moment from "moment";

import Animation from 'lottie-react-native';
const { ReactNativeEventEmitter } = NativeModules;
import APIManager from '../api/APIManager';
import CodePush from "react-native-code-push";

const packetGray = (Platform.OS === 'ios') ? require('../../ios/assets/assets/images/packet_gray.png') : 'asset:/images/packet_gray.png';
const packetColor = (Platform.OS === 'ios') ? require('../../ios/assets/assets/images/packet.png') : 'asset:/images/packet.png';

class Dashboard extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
        loadedChallenge: null,
        isSyncing: false,
        calories: '- - ',
        average: '- -',
        rank: '- -',
        capColor: '#AAAAAA',
        packetImage: packetGray,
        challengeName:"Loading Challenge ...",
        lastSync: 'Last Sync: ...',
        packetsDonated: '- -',
        packetValue: 0
    }
  }

  componentDidMount() {
      const packetsLookup = (Platform.OS === 'ios') ? this.props.User.userId.toString() : this.props.userId.toString() + ":packets";

      AsyncStorage.getItem(packetsLookup).then((packetsDonated) => {

          if (packetsDonated !== null) {
            this.setState({packetsDonated: packetsDonated.split(":")[0], packetValue: parseInt(packetsDonated.split(":")[1])});
          }
          else {
            AsyncStorage.setItem(packetsLookup, "0:0");
          }

      }).then(() => {
          this.getLatestChallenge();
      }).catch((error) => console.log(error));

  }


  getLatestChallenge() {

    APIManager.getLatestChallenge(this.props.baseURL, (Platform.OS === 'ios') ? this.props.User.userId : this.props.userId, (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken)
        .then((response) => response.json())
        .then((responseJson) => {
            this.setState({loadedChallenge: responseJson, challengeName: responseJson["challengeName"]});
            this.getChallengeStats();
        })
        .catch((error) => console.log(error));

  }

  loadChallengeInfo() {
     ReactNativeEventEmitter.loadChallengeInfo(this.props.rootTag, this.state.loadedChallenge);
  }

  getChallengeStats() {

    const packetsLookup = (Platform.OS === 'ios') ? this.props.User.userId.toString() : this.props.userId.toString() + ":packets";

    APIManager.getChallengeSummary(this.props.baseURL, this.state.loadedChallenge["challengeId"], (Platform.OS === 'ios') ? this.props.User.userId : this.props.userId, (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken)
        .then((response) => response.json())
        .then(responseJson => {
            this.setState({calories: responseJson["dailyAverage"], rank: responseJson["rankInTeam"], average: Math.round(responseJson["daysAboveAverage"]), packetsDonated: responseJson["packets"], packetValue: responseJson["nextPacket"] * 100, capColor: '#fdb515', packetImage: packetColor});

            this.checkForPacketIncrease(responseJson["packets"]);
            AsyncStorage.setItem(packetsLookup, responseJson["packets"] + ":" + responseJson["nextPacket"] * 100);
            this.getLastSyncedDate();

        }).catch(error => console.log(error));
  }

  getLastSyncedDate() {

    this.setState({lastSync: 'Last Sync: ...'});

    APIManager.getLastSyncDateDevice(this.props.baseURL, (Platform.OS === 'ios') ? this.props.User.userId : this.props.userId, (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken)
        .then(response => response.json())
        .then(responseJson => {
            this.setState({lastSync: (responseJson['updatedAt'] != '') ? 'Last Sync: ' + moment(responseJson['updatedAt']).utc(true).format('MM.DD.YYYY hh:mma') : 'Last Sync: Not Synced Yet'});
        }).catch((error) => console.log(error));

  }

  checkForPacketIncrease(packets) {

      const packetsLookup = (Platform.OS === 'ios') ? this.props.User.userId.toString() : this.props.userId.toString() + ":packets";

      AsyncStorage.getItem(packetsLookup).then((packetsDonated) => {
          if (packetsDonated !== null) {

              if (packets - parseInt(packetsDonated.split(":")[0]) >= 1) {
                  this.playAnimation();
              }

          }
      }).catch((error) => console.log(error));

  }

  visitDashboardButtonPressed() {
    Linking.canOpenURL(this.props.baseURL + "/#/user-dashboard/accessToken/" + (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken)
        .then(supported => {

            if (supported) {
                Linking.openURL(this.props.baseURL + "/#/user-dashboard/accessToken/" + (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken).catch(error => console.log(error));
            }
            else {
                console.log("Cannot open url");
            }

        }).catch(error => console.log(error));
  }

  playAnimation() {
    this.animation.play();
  }

  render() {
    return (
      <ScrollView bounces={false}>
          <TouchableOpacity onPress={this.loadChallengeInfo.bind(this)} style={styles.challengeSection}>
              <Text numberOfLines={2} style={styles.challengeTitle}>
                  {this.state.challengeName}
              </Text>
              <Image style={{height: 20, width: 20, resizeMode: "contain", tintColor:'#DBDBDB', marginLeft: 8}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/angle.png') : {uri: 'asset:/images/angle.png'}}/>
          </TouchableOpacity>
          <View style={styles.packetContainer}>
              <Animation
                  ref={animation => {
                      this.animation = animation;
                  }}
                  style={{
                      position: 'absolute',
                      width: "100%",
                      height: "100%"
                  }}
                  source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/animation/hearts.json') : "images/hearts.json"} />
              <View style={styles.syncContainer}>
                <TouchableOpacity onPress={this.getLastSyncedDate.bind(this)}>
                    <Image source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/sync.png') : {uri: 'asset:/images/sync.png'}} style={{height: 20, width: 20}}/>
                </TouchableOpacity>
                  <Text style={styles.syncText}>{this.state.lastSync}</Text>
              </View>
              <View style={styles.centeredContainer}>
                  <AFGAnimatedCircularProgress
                      size={180}
                      width={14}
                      rotation={180}
                      fill={this.state.packetValue}
                      capWidth={15}
                      capColor={this.state.capColor}
                      prefill={0}
                      tintColor="#f44f43"
                      strokeCap="round"
                      children={
                          (fill) => (
                              <Image style={{height: 115, width: 115, resizeMode: 'contain'}} source={(Platform.OS === 'ios') ? this.state.packetImage : {uri: this.state.packetImage}}/>
                          )
                      }
                      backgroundColor="#CECECE" />

                  <Text style={styles.donatedPacketsHeader}>
                      {/*<AFGCounter ref={counter => { this.counter = counter; }} end={this.state.packetsDonated} start={0} time={600} digits={0} easing="linear" style={styles.donatedPacketsValue} />*/}
                      <Text style={styles.donatedPacketsValue}>{this.state.packetsDonated}</Text> Packets Donated
                   </Text>
                  <Text style={styles.donatedPacketsSubheader}>(500 active calories = 1 packet donated)</Text>
              </View>
          </View>
          <View style={styles.statsContainer}>

              {/* Calories */}
              <View style={styles.statBox}>
                  <Image style={{height: 22, width: 15}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/calories.png') : {uri: 'asset:/images/calories.png'}} />
                  <Text style={styles.statValue}>{this.state.calories}</Text>
                  <Text style={styles.statTitle}>CALORIES</Text>
                  <Text style={styles.statDisclaimer}>(my daily average)</Text>
              </View>

              {/* Average */}
              <View style={[styles.statBox, {borderLeftColor: '#BBBBBB', borderLeftWidth: 0.5}]}>
                  <Image style={{height: 19, width: 20}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/above.png') : {uri: 'asset:/images/above.png'}} />
                  <Text style={styles.statValue}>{this.state.average}</Text>
                  <Text style={styles.statTitle}>ABOVE AVG.</Text>
                  <Text style={styles.statDisclaimer}>(my days above average)</Text>
              </View>

              {/* Rank */}
              <View style={[styles.statBox, {borderLeftColor: '#BBBBBB', borderLeftWidth: 0.5}]}>
                  <Image style={{height: 19, width: 18}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/rank.png') : {uri: 'asset:/images/rank.png'}} />
                  <Text style={styles.statValue}>{this.state.rank}</Text>
                  <Text style={styles.statTitle}>RANK</Text>
                  <Text style={styles.statDisclaimer}>(on my team)</Text>
              </View>

          </View>
          <View style={{justifyContent: 'center', alignItems: 'center', marginTop: 20}}>
              <TouchableHighlight underlayColor="#FF9800" onPress={this.visitDashboardButtonPressed.bind(this)} style={styles.visitDashboardButton}>
                  <Text style={styles.buttonText}>VISIT DASHBOARD</Text>
              </TouchableHighlight>
              <Text style={styles.dashboardText}>To view progress, visit the challenge{'\n'}dashboard (will open in a web browser)</Text>
          </View>
      </ScrollView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  challengeSection: {
    flex: 1,
    justifyContent: 'center',
    alignItems:'center',
    flexDirection: 'row',
    padding: 15
  },
  challengeTitle: {
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold',
    fontSize: 17,
    paddingLeft: 5,
    paddingRight: 5,
    color: '#515B61'
  },
  centeredContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1
  },
  syncContainer: {
    flex: 1,
    padding: 15,
    flexDirection: 'row'
  },
  syncText: {
    fontSize: 14,
    color: '#515B61',
    paddingLeft: 10,
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-It' : 'SourceSansPro_It'
  },
  packetContainer: {
    flex: 1,
    backgroundColor: '#EBEBEB'
  },
  donatedPacketsHeader: {
    paddingTop: 5,
    fontSize: 20,
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Regular' : 'SourceSansPro_Regular',
    color: '#515B61'
  },
  donatedPacketsValue: {
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Semibold' : 'SourceSansPro_Semibold',
    color: '#f44f43',
    fontSize: 36
  },
  donatedPacketsSubheader: {
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-It' : 'SourceSansPro_It',
    fontSize: 15,
    color: '#BBBBBB',
    paddingBottom: 15
  },
  statsContainer: {
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#BDBDBD',
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  statBox: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center'
  },
  statValue: {
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Light' : 'SourceSansPro_Light',
    color: '#515B61',
    fontSize: 32
  },
  statTitle: {
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Light' : 'SourceSansPro_Light',
    color: '#515B61',
    fontSize: 16
  },
  statDisclaimer: {
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-LightIt' :'SourceSansPro_LightIt',
    color: '#BBBBBB',
    fontSize: 10
  },
  visitDashboardButton: {
    width: 308,
    height: 48,
    backgroundColor: '#FDB515',
    justifyContent: 'center',
    borderRadius: 5
  },
  buttonText: {
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold',
    fontSize: 18,
    color: '#FFF'
  },
  dashboardText: {
    paddingTop: 10,
    textAlign: 'center',
    fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-It' : 'SourceSansPro_It',
    color: '#515B61',
    paddingBottom: 10,
    fontSize: 14
  }
});

Dashboard = CodePush({checkFrequency: CodePush.CheckFrequency.ON_APP_START, installMode: CodePush.InstallMode.IMMEDIATE})(Dashboard);
module.exports = Dashboard;
