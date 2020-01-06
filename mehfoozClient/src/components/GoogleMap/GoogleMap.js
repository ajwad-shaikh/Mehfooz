import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import { geolocated } from 'react-geolocated';
import { Map, GoogleApiWrapper, Marker } from 'google-maps-react';

const styles = theme => ({
  buttonIcon: {
    marginRight: theme.spacing(1),
  },
  mapHome: {
    marginTop: '20px',
    marginLeft: 'auto',
    marginRight: 'auto',
  },
});

class GoogleMap extends Component {
  constructor(props) {
    super(props);

    this.state = {
      center: {
        lat: 23.0168,
        lng: 80.9558,
      },
      zoom: 6,
      markerPosition: {
        lat: 23.0168,
        lng: 80.9558,
      },
    };
  }

  render() {
    // Styling
    // const { classes } = this.props;

    // Properties
    const { mapClicked, markerPosition } = this.props;

    const { zoom } = this.state;

    const mapStyles = {
      marginTop: '20px',
      width: '40%',
      height: '50%',
    };

    return (
      <Map
        google={this.props.google}
        zoom={zoom}
        containerStyle={mapStyles}
        onClick={(e, f, g) => mapClicked(e, f, g)}
        centerAroundCurrentLocation
      >
        {this.props.isGeolocationAvailable &&
          this.props.isGeolocationEnabled &&
          this.props.coords && (
            <Marker
              key={markerPosition}
              position={{ lat: markerPosition.lat, lng: markerPosition.lng }}
            />
          )}
      </Map>
    );
  }
}

GoogleMap.propTypes = {
  // Styling
  classes: PropTypes.object.isRequired,

  // Properties
  user: PropTypes.object,
};

export default withRouter(
  withStyles(styles)(
    GoogleApiWrapper({
      apiKey: process.env.REACT_APP_MAP_KEY,
    })(
      geolocated({
        positionOptions: {
          enableHighAccuracy: true,
          maximumAge: 0,
          timeout: Infinity,
        },
        watchPosition: false,
        userDecisionTimeout: null,
        suppressLocationOnMount: false,
        geolocationProvider: navigator.geolocation,
        isOptimisticGeolocationEnabled: true,
      })(GoogleMap),
    ),
  ),
);
