import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { firestore } from '../../firebase';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const styles = theme => ({
  buttonIcon: {
    marginRight: theme.spacing(1),
  },
  formGrid: {
    height: '55vh',
    marginLeft: '0px',
    marginRight: '0px',
  },
  header: {
    marginTop: '20px',
    justifyContent: 'center',
    alignItems: 'center',
  },
  mapGrid: {
    marginTop: '75px',
  },
  formControl: {
    margin: theme.spacing(0),
  },
  tabHeader: {
    width: '80%',
    marginLeft: 'auto',
    marginRight: 'auto',
    justifyContent: 'center',
    alignItems: 'center',
  },
  performingAction: false,
});

class CompletedTable extends Component {
  constructor(props) {
    super(props);

    this.state = {
      performingAction: false,

      tabValue: 0,
      completedList: [],
    };
  }

  fetchCompletedDistress() {
    var list = [];
    firestore
      .collection('completed')
      .get()
      .then(snapshot => {
        snapshot.forEach(doc => {
          let distress = doc.data();
          distress.id = doc.id;
          list.push(distress);
        });
        this.setState({
          completedList: list,
        });
      });
  }

  render() {
    // Styling
    const { classes } = this.props;

    // Properties
    // const { user, markerList } = this.props;

    const { completedList } = this.state;

    return (
      <TableContainer component={Paper}>
        <Table className={classes.table} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>User Name</TableCell>
              <TableCell align="center">Timestamp</TableCell>
              <TableCell align="center">Location</TableCell>
              <TableCell align="center">Contact Number</TableCell>
              <TableCell align="center">Distress Type</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {completedList.map(row => {
              return (
                <TableRow key={row.id}>
                  <TableCell component="th" scope="row">
                    {row.user.name}
                  </TableCell>
                  <TableCell align="center">
                    {new Date().toISOString()}
                  </TableCell>
                  <TableCell align="center">
                    {JSON.stringify(row.location)}
                  </TableCell>
                  <TableCell align="center">{row.user.contactNumber}</TableCell>
                  <TableCell align="center">{row.user.type}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    );
  }

  componentDidMount() {
    this.fetchCompletedDistress();
  }
}

CompletedTable.propTypes = {
  // Styling
  classes: PropTypes.object.isRequired,

  // Properties
  user: PropTypes.object,
};

export default withRouter(withStyles(styles)(CompletedTable));
