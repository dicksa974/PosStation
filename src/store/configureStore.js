import { createStore } from 'redux';
import toggleLogin from './reducers/loginReducer';
import toggleTicket from './reducers/ticketReducer';
import { persistCombineReducers } from 'redux-persist';
import AsyncStorage from '@react-native-community/async-storage';

const rootPersistConfig = {
    key : 'root',
    storage : AsyncStorage
};

//export default createStore(persistCombineReducers(rootPersistConfig, {toggleLogin, toggleTransaction}));
export default createStore(persistCombineReducers(rootPersistConfig, {toggleLogin, toggleTicket }));
