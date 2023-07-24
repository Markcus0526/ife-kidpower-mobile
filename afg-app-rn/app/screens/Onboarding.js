import React from 'react';
import { StyleSheet, Text, Image, NativeModules, NativeEventEmitter, ScrollView, Platform, View } from 'react-native';
import Swiper from 'react-native-swiper';
import moment from 'moment';
import CodePush from "react-native-code-push";
const { ReactNativeEventEmitter } = NativeModules;

class Onboarding extends React.Component {

    constructor(props) {
        super(props);
        this._subscription = null;
    }

    componentDidMount() {
        const emitter = new NativeEventEmitter(ReactNativeEventEmitter);
        this._subscription = emitter.addListener('OnboardingEvent', () => (this.swiper.state.index < this.swiper.state.total - 1) ? this.swiper.scrollBy(1) : ReactNativeEventEmitter.dismissPresentedViewController(this.props.rootTag));
    }

    componentWillUnmount() {
        this._subscription.remove();
    }

    render() {
        var startDate = moment(this.props.startDate).utc(false).format('DD MMMM YYYY');
        var endDate = moment(this.props.endDate).utc(false).format('DD MMMM YYYY');

        return (
            <Swiper ref={swiper => { this.swiper = swiper; }} showsPagination={(Platform.OS === 'ios' ? true : false)} activeDotColor='#515B61' style={styles.wrapper} showsButtons={false} autoplay={false} loop={false} onIndexChanged={(index) => ReactNativeEventEmitter.updateUI(this.props.rootTag, index, this.swiper.state.total)}>
                <ScrollView bounces={false} centerContent={true} contentContainerStyle={styles.first}>
                    <View style={styles.container}>
                        <View style={styles.logoContainer}>
                            <Image source={{uri: (this.props.imageSrc != '') ? this.props.imageSrc : this.props.organization.imageSrc }} style={{height: 200, width: 300, resizeMode: 'contain'}} />
                        </View>
                        <Text style={styles.headerStyle}>
                            You've Been Challenged!
                        </Text>
                        <Text style={styles.challengeTitle}>
                            {this.props.name}
                        </Text>
                        <Text style={styles.challengeDates}>
                            {startDate} - {endDate}
                        </Text>
                        <Text style={styles.donationRate}>
                            Donation Rate - 1:{this.props.impactMultiplier}
                        </Text>
                        <Text style={styles.donationInfo}>
                            {(Platform.OS === 'ios') ? this.props.organization.name : this.props.organizationName} is donating {this.props.impactMultiplier} {(this.props.brand == 'unicef') ? (this.props.impactMultiplier > 1) ? 'packets' : 'packet' : (this.props.impactMultiplier > 1) ? 'calories': 'calorie'} of therapeutic food to a child in need for every {(this.props.brand == 'unicef') ? '5 Power Points' : '1 active calorie'} you {(this.props.brand == 'unicef') ? 'earn' : 'burn'} in this challenge.
                        </Text>
                    </View>
                </ScrollView>
                <View>
                    <Image style={{height: 300, width: null}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/Step2.png') : {uri: 'asset:/images/Step2.png'}}/>
                    <Text style={styles.headerStyle}>
                        How It Works
                    </Text>
                    <Text style={styles.info}>
                        The {(this.props.brand == 'unicef') ? 'Power Points' : 'active calories'} you {(this.props.brand == 'unicef') ? 'earn' : 'burn'} during the challenge will be sponsored by {(Platform.OS === 'ios') ? this.props.organization.name : this.props.organizationName} and converted into life-saving food packets for malnourished kids.
                    </Text>
                    <Text style={styles.slogan}>It's time to Get Active For Good!</Text>

                </View>
                <View>
                    <Image style={{height: 300, width: null}} source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/Step3.png') : {uri: 'asset:/images/Step3.png'}}/>
                    <Text style={styles.headerStyle}>
                        Three Easy Steps to Join
                    </Text>
                    <View style={{width: 250, marginLeft: 'auto', marginRight: 'auto'}}>
                        <View style={styles.stepsContainer}>
                            <Image style={{height:28, width: 28}} resizeMode="contain" source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/1.png') : {uri: 'asset:/images/1.png'}} />
                            <Text style={styles.steps}>Create an account or log in</Text>
                        </View>
                        <View style={styles.stepsContainer}>
                            <Image style={{height:28, width: 28}} resizeMode="contain" source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/2.png') : {uri: 'asset:/images/2.png'}} />
                            <Text style={styles.steps}>Select a Team and/or Region</Text>
                        </View>
                        <View style={styles.stepsContainer}>
                            <Image style={{height:28, width: 28}} resizeMode="contain" source={(Platform.OS === 'ios') ? require('../../ios/assets/assets/images/3.png') : {uri: 'asset:/images/3.png'}} />
                            <Text style={styles.steps}>Connect an Activity Tracker</Text>
                        </View>
                    </View>
                </View>
            </Swiper>
        );
    }
}

const styles = StyleSheet.create({
    first: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center'
    },
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#FFFFFF',
    },
    logoContainer: {
        alignItems: 'center',
        justifyContent: 'center'
    },
    headerStyle: {
        fontSize: 26,
        textAlign: 'center',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Light' : 'SourceSansPro_Light',
        color: '#515B61',
        paddingTop: 30,
        paddingBottom: 30
    },
    challengeTitle: {
        textAlign: 'center',
        fontSize: 17,
        color: '#515B61',
        paddingLeft: 30,
        paddingRight: 30,
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold'
    },
    challengeDates: {
        textAlign: 'center',
        fontSize: 16,
        color: '#515B61',
        fontFamily: 'SourceSansPro-It'
    },
    donationRate: {
        paddingTop: 40,
        textAlign: 'center',
        fontSize: 17,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Bold' : 'SourceSansPro_Bold'
    },
    donationInfo: {
        paddingLeft: 20,
        paddingRight: 20,
        textAlign: 'center',
        fontSize: 16,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Regular' : 'SourceSansPro_Regular'
    },
    info: {
        textAlign: 'center',
        fontSize: 16,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Regular' : 'SourceSansPro_Regular',
        paddingLeft: 30,
        paddingRight: 30,
        paddingBottom: 20
    },
    slogan: {
        textAlign: 'center',
        fontSize: 18,
        color: '#515B61',
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-BoldIt' : 'SourceSansPro_BoldIt'
    },
    stepsContainer: {
        flexDirection: 'row',
        paddingBottom: 20
    },
    steps: {
        fontFamily: (Platform.OS === 'ios') ? 'SourceSansPro-Regular' : 'SourceSansPro_Regular',
        fontSize: 18,
        paddingLeft: 10,
        color: '#515B61'
    }

});

Onboarding = CodePush({checkFrequency: CodePush.CheckFrequency.ON_APP_START, installMode: CodePush.InstallMode.IMMEDIATE})(Onboarding);
module.exports = Onboarding;
