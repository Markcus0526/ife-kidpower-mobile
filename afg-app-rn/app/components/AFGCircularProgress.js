import React from 'react';
import PropTypes from 'prop-types';
import { View, Platform, ViewPropTypes, ART } from 'react-native';
const { Surface, Shape, Path, Group } = ART;
import MetricsPath from 'art/metrics/path';

export default class AFGCircularProgress extends React.Component {

    circlePath(cx, cy, r, startDegree, endDegree) {

        let p = Path();
        p.path.push(0, cx + r, cy);
        p.path.push(4, cx, cy, r, startDegree * Math.PI / 180, endDegree * Math.PI / 180, 1);

        return p;
    }

    extractFill(fill) {
        return Math.min(100, Math.max(0, fill));
    }

    render() {
        const { size, width, tintColor, backgroundColor, style, rotation, children, capWidth, capColor, strokeCap } = this.props;
        const borderWidth = capWidth > width ? capWidth : width;
        const radius = (size-borderWidth)/2;
        const center = size/2;
        const backgroundPath = (Platform.OS === 'ios') ? this.circlePath(center, center, radius, 0, 360) : this.circlePath(center, center, center - width / 2, 1, 360);

        const fill = this.extractFill(this.props.fill);
        const circlePath = this.circlePath(center, center, radius, 0, 360 * fill / 100);
        const offset = size - (width * 2);

        const radian = Math.PI * fill/50;
        const capX = radius * Math.cos(radian) + center;
        const capY = radius * Math.sin(radian) + center;

        return (
            <View style={style}>
                <Surface
                    width={size}
                    height={size}>
                    <Group rotation={rotation - 90} originX={center} originY={center}>
                        <Shape d={backgroundPath}
                               stroke={backgroundColor}
                               fill='#FFF'
                               strokeWidth={width-7}/>
                        <Shape d={circlePath}
                               stroke={tintColor}
                               strokeWidth={width}
                               strokeCap={strokeCap}/>
                        <Shape d={this.circlePath(capX, capY, capWidth/4, (Platform.OS === 'ios') ? 0 : 1, 360)}
                               stroke={capColor}
                               strokeWidth={capWidth/2}/>
                    </Group>
                </Surface>
                {children &&
                <View
                    style={{
                        position: 'absolute',
                        left: width,
                        top: width,
                        width: offset,
                        height: offset,
                        borderRadius: offset / 2,
                        alignItems: 'center',
                        justifyContent: 'center'
                    }}>
                    {children(fill)}
                </View>}
            </View>
        )
    }
}

AFGCircularProgress.propTypes = {
    style: ViewPropTypes.style,
    size: PropTypes.number.isRequired,
    fill: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    strokeCap: PropTypes.string,
    capColor: PropTypes.string,
    capWidth: PropTypes.number,
    tintColor: PropTypes.string,
    backgroundColor: PropTypes.string,
    rotation: PropTypes.number,
    children: PropTypes.func
};

AFGCircularProgress.defaultProps = {
    tintColor: 'black',
    backgroundColor: '#e4e4e4',
    rotation: 90,
    strokeCap: 'butt',
    capColor: 'black',
    capWidth: 0
};
