import React from 'react'
import PropTypes from 'prop-types'
import { isEmpty, isUndefined } from 'lodash'

const CategoryItemMatrix = ({
  itemTitle,
  itemName,
  wordCount,
  ...props
}) => {
  const title = isEmpty(itemTitle) || isUndefined(itemTitle) ||
    itemTitle === 'null' ? '' : itemTitle
  const name = isEmpty(itemName) || isUndefined(itemName) || itemName === 'null'
    ? 'N/A' : itemName
  return (
    <tr>
      <td className='l--pad-left-0 l--pad-v-0 w--1'>
        {title} <span className='txt--understated'>({name})</span>
      </td>
      <td className='txt--align-right l--pad-right-0 l--pad-v-0 txt--nowrap'>
        {wordCount} <span className='l--pad-left-quarter txt--understated'>
        words</span>
      </td>
    </tr>
  )
}

CategoryItemMatrix.propTypes = {
  itemTitle: PropTypes.string,
  itemName: PropTypes.string,
  wordCount: PropTypes.number
}

export default CategoryItemMatrix
