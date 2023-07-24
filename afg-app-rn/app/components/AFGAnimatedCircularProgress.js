import React from 'react';
import PropTypes from 'prop-types';
import { View, Animated, ViewPropTypes, Easing } from 'react-native';
import AFGCircularProgress from './AFGCircularProgress';
const AnimatedProgress = Animated.createAnimatedComponent(AFGCircularProgress);

export default class AFGAnimatedCircularProgress extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            chartFillAnimation: new Animated.Value(props.prefill || 0)
        }
    }

    componentDidMount() {
        this.animateFill();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.fill !== this.props.fill) {
            this.animateFill();
        }
    }

    animateFill() {
        const { tension, friction, onAnimationComplete } = this.props;

        Animated.spring(
            this.state.chartFillAnimation,
            {
                toValue: this.props.fill,
                tension,
                friction
            }
        ).start(onAnimationComplete);
    }

    performLinearAnimation(toValue, duration) {
        const { onLinearAnimationComplete } = this.props;

        Animated.timing(this.state.chartFillAnimation, {
            toValue: toValue,
            easing: Easing.linear,
            duration: duration
        }).start(onLinearAnimationComplete);
    }

    render() {
        const { fill, prefill, ...other } = this.props;

        return (
            <AnimatedProgress
                {...other}
                fill={this.state.chartFillAnimation}
            />
        )
    }
}

AFGAnimatedCircularProgress.propTypes = {
    style: ViewPropTypes.style,
    size: PropTypes.number.isRequired,
    fill: PropTypes.number,
    prefill: PropTypes.number,
    width: PropTypes.number.isRequired,
    tintColor: PropTypes.oneOfType([PropTypes.string, PropTypes.object]),
    backgroundColor: PropTypes.oneOfType([PropTypes.string, PropTypes.object]),
    tension: PropTypes.number,
    friction: PropTypes.number,
    onAnimationComplete: PropTypes.func,
    onLinearAnimationComplete: PropTypes.func
};

AFGAnimatedCircularProgress.defaultProps = {
    tension: 7,
    friction: 10
};
