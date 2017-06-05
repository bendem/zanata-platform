import React, { PropTypes } from 'react'
import { Icon } from '../../../components'

/**
 * Generic panel showing an icon and message, to
 * use when there are no suggestions to display.
 */
class NoSuggestionsPanel extends React.Component {
  static propTypes = {
    message: PropTypes.string.isRequired,
    icon: PropTypes.oneOf(['search', 'suggestions']).isRequired
  }

  render () {
    return (
      <div
        className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          <Icon name={this.props.icon} className="s5" />
        </div>
        <p>{this.props.message}</p>
      </div>
    )
  }
}

export default NoSuggestionsPanel
