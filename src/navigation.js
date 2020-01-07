import React from 'react';
import { View, StyleSheet, Text, ScrollView, TouchableOpacity } from 'react-native';
import { createSwitchNavigator, createStackNavigator, createAppContainer } from 'react-navigation';

// #SCREEN
import SplashScreen from './screens/SplashScreen';
import Home from './screens/Home';
import newOrder from './screens/pay/newOrder';
import infoUser from './screens/infoUser';
import listTransactions from './screens/listTransactions';
import SignIn from './screens/signIn';
import saisieCarte from './screens/pay/saisieCarte';
import saisieRechargeCarte from './screens/recharge/saisieRechargeCarte';
import saisieMontant from './screens/recharge/saisieMontant';
import saisieActivationCarte from './screens/activation/saisieActivationCarte';

const HomeStack = createStackNavigator({
    Home: { screen : Home, navigationOptions:{ header: null }},
    NewOrder: newOrder,
    InfoUser: infoUser,
    ListTransactions: listTransactions,
    SaisieCarte: saisieCarte,
    SaisieRechargeCarte: saisieRechargeCarte,
    SaisieMontant: saisieMontant,
    SaisieActivationCarte: saisieActivationCarte,
});

const AuthStack = createStackNavigator({
    SignIn: {
        screen: props => <SignIn navigation={props.navigation} { ...props.screenProps } />
    }}, {
    headerMode: 'none'
});

export default createAppContainer(createSwitchNavigator({
        AuthLoading: {
            screen: props => <SplashScreen navigation={props.navigation} { ...props.screenProps } />
        },
        Auth: AuthStack,
        MyApp: HomeStack
    },
    {
        headerMode: 'none',
        initialRouteName: 'AuthLoading',
    }));
