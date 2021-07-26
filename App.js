/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';

import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import { NativeModules } from 'react-native';



class Main extends React.Component {
  state = {
    serNumb: '......',
    name: "A920",

    type: "Pro",

    title: "Demo",
    hQ: "Print",

  }
  getSerialNum = async () => {
    console.log("pos");
    const { PrintModule } = NativeModules;
    await PrintModule.getSerialNumber(
      (pos) => {
        console.log("pos", pos);
        alert(pos)
        this.setState({ serNumb: pos }, () => {
          console.log("pos", pos);
        });
      },
      (fail) => { console.log(fail) },
    );
  };
  handlePrint = () => {
    const { name,

      type,

      title,
      hQ,
    } = this.state
    const { PrintModule } = NativeModules;
    PrintModule.PrintReceipt(
      name,
      type,
      title,
      hQ,
      success => {
        alert("Your Reciept is printed");
      },
      fail => { alert(fail) },
    );
  }
  render() {

    return (

      <ScrollView>

        <View style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', marginTop: 200 }}>
          <Text> Serial Number  :</Text>
          <Text style={{ color: 'red' }}>{this.state.serNumb}</Text>
          <TouchableOpacity style={{
            margin: 20, backgroundColor: '#489629',
            fontSize: 22,
            // margin: 6,
            width: 200,
            height: 50,
            padding: 5,
            display: 'flex',
            justifyContent: 'center',
            borderRadius: 5,

          }} onPress={() => this.getSerialNum()}>
            <Text>Press  To Show Serial Number</Text>

          </TouchableOpacity>
          <TouchableOpacity style={{
            backgroundColor: '#489629',
            fontSize: 22,
            margin: 6,
            width: 200,
            height: 50,
            padding: 5,
            display: 'flex',
            justifyContent: 'center',
            borderRadius: 5,
            alignItems: 'center'
          }} onPress={() => this.handlePrint()}>
            <Text>Press  To Print A Reciept</Text>

          </TouchableOpacity>
        </View>
      </ScrollView>

    );
  }

};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default Main;
