/**
 * Created by Adeline B. on 15/05/2018.
 */
import React from 'react';
import PropTypes from 'prop-types';
import { Keyboard } from 'react-native';
import Prompt from 'rn-prompt';

export default class MFAPrompt extends React.Component {
  static propTypes = {
    onValidate: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    onSuccess: PropTypes.func.isRequired,
  };

  static defaultProps = {
    onValidate: () => null,
    onCancel: () => null,
    onSuccess: () => null,
  };

  constructor(props) {
    super(props);

    this.handleCancel = this.handleCancel.bind(this);
    this.handleValidateMFACode = this.handleValidateMFACode.bind(this);
  }

  state = {
    promptTitle: 'Enter code',
    code: '',
  }

  handleCancel() {
    Keyboard.dismiss();
    this.props.onCancel();
  }

  async handleValidateMFACode(code) {
    try {
      const validate = await this.props.onValidate(code);
      const validCode = validate === true;
      const promptTitle = validCode ?
        'Enter code' :
        `${validate} Enter code again`;

      this.setState({
        promptTitle
      }, () => {
        if (validCode) {
          this.props.onSuccess();
        }
      });
    } catch (err) {
      this.setState({ promptTitle: `${err.message} Enter code again` });
    }

  }

  render() {
    return (
      <Prompt
        title={this.state.promptTitle}
        placeholder="Code"
        textInputProps={{
          keyboardType: 'numeric',
        }}
        visible={true}
        onCancel={this.handleCancel}
        onSubmit={this.handleValidateMFACode}
      />
    );
  }

}