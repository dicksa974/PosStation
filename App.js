import React from 'react';
import { View } from 'react-native';
import Navigation from "./src/navigation";

import { Provider } from 'react-redux';
import Store from './src/store/configureStore';
import { persistStore } from 'redux-persist';
import { PersistGate } from 'redux-persist/es/integration/react';

class App extends React.Component {
  render() {
    let persistor = persistStore(Store);
    return (
      <Provider store={Store}>
        <PersistGate persistor={persistor}>
          <View style={{flex: 1, backgroundColor: '#fff'}}>
            <Navigation/>
          </View>
        </PersistGate>
      </Provider>
    )
  }
}

export default App;
