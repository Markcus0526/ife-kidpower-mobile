import React from 'react';
import {View, Text, Image, NativeModules, NativeEventEmitter, StyleSheet, FlatList, Alert, TouchableOpacity, Platform} from 'react-native';
const { ReactNativeEventEmitter } = NativeModules;
import APIManager from '../api/APIManager';
import moment from "moment";
const trackers = (Platform.OS === 'ios') ? require('../config/trackers-ios.json') : require('../config/trackers-android.json');
import CodePush from "react-native-code-push";

class Tracker extends React.Component {

    constructor(props) {
        super(props);
        this.state = {lastSynced: (!this.props.fromOnboarding) ? 'Last Sync: 00.00.0000 00:00' : 'Use phone as tracker'};
        if (Platform.OS != 'ios') {
          this._subscription = null;
        }
    }

    componentWillUnmount() {
      if (Platform.OS != 'ios') {
        this._subscription.remove();
      }
    }

    getLastSyncedDate() {

        APIManager.getLastSyncDateDevice(this.props.baseURL, (Platform.OS === 'ios') ? this.props.User.userId : this.props.userId, (Platform.OS === 'ios') ? this.props.User.accessToken : this.props.accessToken)
            .then(response => response.json())
            .then(responseJson => {
                this.setState({lastSynced: (responseJson['updatedAt'] != '') ? 'Last Sync: ' + moment(responseJson['updatedAt']).utc(true).format('MM.DD.YYYY hh:mma') : 'Last Sync: Not Synced Yet'});
            }).catch((error) => console.log(error));

    }


    componentDidMount() {

      if (Platform.OS != 'ios') {
        const emitter = new NativeEventEmitter(ReactNativeEventEmitter);
        this._subscription = emitter.addListener('TrackerEvent', () => this.getLastSyncedDate().bind(this));
      }

      if (!this.props.fromOnboarding) {
          this.getLastSyncedDate();
      }
    }

    FlatListItemSeparator = () => {
        return (
            <View
                style={{
                    height: 1,
                    width: "100%",
                    backgroundColor: "#BBBBBB"
                }}
            />
        );
    };

    _onPressItem = (id) => {
        // Phone tracker is NOT enabled
        if (!this.props.phoneTrackerEnabled) {
            // Are we in the onboarding screen?
            if (this.props.fromOnboarding) {
                Alert.alert((Platform.OS === 'ios') ? "Connect to Apple Health" : "Connect to Google Fit", "We would like to pull active calories from your " + (Platform.OS === 'ios') ? "iPhone." : "phone." + "You can always change your settings later",
                    [
                        {text: 'Cancel', onPress: () => console.log("Cancel")}, {text: 'Continue', onPress: () => ReactNativeEventEmitter.loadHealthAuthScreen(this.props.rootTag, (Platform.OS === 'ios') ? this.props.fromOnboarding ? 1 : 0 : this.props.fromOnboarding)},
                    ], { cancelable: false });

            }
            // From the 'Tracker' screen
            else {
                ReactNativeEventEmitter.loadHealthAuthScreen(this.props.rootTag, (Platform.OS === 'ios') ? this.props.fromOnboarding ? 1 : 0 : this.props.fromOnboarding)
            }
        }
        // Phone tracker IS enabled
        else {
          this.getLastSyncedDate();
        }
    };

    render() {
        return (
            <View>
                <Text style={[styles.trackerSectionTitle, {paddingTop: this.props.fromOnboarding ? 0 : 20}]}>Connect Your Phone as a Tracker</Text>
                <Text style={styles.trackerSectionDescription}>You can add or change trackers online later</Text>
                <FlatList data={trackers} extraData={this.state} ItemSeparatorComponent={this.FlatListItemSeparator} renderItem={({item}) => (
                    <TouchableOpacity onPress={this._onPressItem.bind(this)}>
                        <View
                            style={{
                                height: 1,
                                width: "100%",
                                backgroundColor: "#BBBBBB"
                            }}
                        />
                        <View style={[styles.trackerContainer, {backgroundColor: this.props.phoneTrackerEnabled ? '#edf9e1' : '#FFF'}]}>
                            <Image style={{height: 40, width: 30, resizeMode: "contain", marginLeft: 20}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/iPhone.png') : {uri: 'asset:/images/iPhone.png'}} />
                            <View style={{flexDirection: 'column', marginRight: 50}}>
                                <Text style={styles.trackerName}>{item.name}</Text>
                                <Text style={styles.trackerInfo}>{(this.props.phoneTrackerEnabled) ? this.state.lastSynced : item.info}</Text>
                            </View>
                            <Image style={{height: 23, width: 23, resizeMode: "contain", marginRight: 20}} source={(this.props.phoneTrackerEnabled) ? (Platform.OS === 'ios') ? require('../../ios/assets/assets/images/check.png') : {uri:'asset:/images/check.png'} : (Platform.OS === 'ios') ? require('../../ios/assets/assets/images/add.png') : {uri: 'asset:/images/add.png'}}/>
                        </View>
                        <View
                            style={{
                                height: 1,
                                width: "100%",
                                backgroundColor: "#BBBBBB"
                            }}
                        />
                    </TouchableOpacity>
                )}/>
            </View>

        );

    }

}

const styles = StyleSheet.create({
    trackerContainer: {
        width: "100%",
        height: 85,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between'
    },
    trackerSectionTitle: {
        textAlign: 'center',
        fontSize: 17,
        color: '#515B61',
        paddingLeft: 10,
        paddingRight: 10,
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold'
    },
    trackerSectionDescription: {
        paddingLeft: 10,
        paddingRight: 10,
        textAlign: 'center',
        paddingBottom: 20,
        fontSize: 16,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Regular' : 'SourceSansPro_Regular'
    },
    trackerName: {
        color: '#515B61',
        fontSize: 17,
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold'
    },
    trackerInfo: {
        fontSize: 16,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-It' : 'SourceSansPro_It'
    }

});

Tracker = CodePush({checkFrequency: CodePush.CheckFrequency.ON_APP_START, installMode: CodePush.InstallMode.IMMEDIATE})(Tracker);
module.exports = Tracker;
