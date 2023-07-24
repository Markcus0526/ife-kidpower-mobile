import { AppRegistry } from 'react-native';
import Dashboard from './app/screens/Dashboard';
import Onboarding from './app/screens/Onboarding';
import Tracker from './app/screens/Tracker';

AppRegistry.registerComponent('Onboarding', () => Onboarding);
AppRegistry.registerComponent('Dashboard', () => Dashboard);
AppRegistry.registerComponent('Tracker', () => Tracker);
