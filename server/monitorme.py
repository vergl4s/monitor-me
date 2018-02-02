import binascii
import os

from flask import Flask
from flask import request, render_template, jsonify

app = Flask(__name__)

app.config['TEMPLATES_AUTO_RELOAD'] = True


TOKENS = []
POSITIONS = {}
# positions = {'Home': {'longitude': 150.675463, 'latitude': -33.747360}}


##################
# Web endpoints
##################

@app.route('/monitor', methods=['GET'])
def monitor_no_token():
    tokens = request.args.get('token')
    tokens = tokens.split(',') if tokens else []

    positions = []

    for token in tokens:
        if token in TOKENS:
            if len(POSITIONS[token]) > 0:
                positions.append(POSITIONS[token][-1])
    
    return render_template('monitor.html', positions=positions, tokens=tokens)


@app.route('/monitor/position', methods=['GET'])
def get_position():

    tokens = request.args.get('token')
    tokens = tokens.split(',') if tokens else []

    positions = []
    
    for token in tokens:
        if token in TOKENS:
            if len(POSITIONS[token]) > 0:
                positions.append(POSITIONS[token][-1])

    return jsonify(positions)


##################
# Mobile endpoints
##################

@app.route('/m/register', methods=['POST'])
def register():
    received_data = request.get_json()
    
    if 'token' in received_data:
        if received_data['token'] in TOKENS:

            return jsonify({"token": received_data['token']}), 200

    token = str(binascii.hexlify(os.urandom(16)), 'ascii')

    TOKENS.append(token)
    POSITIONS[token] = []

    return jsonify({"token":token}), 200


@app.route('/m/unregister', methods=['POST'])
def unregister():
    received_data = request.get_json()
    print(received_data)
    print(TOKENS)
    
    if 'token' in received_data:
        if received_data['token'] not in TOKENS:
            return jsonify({}), 500
        else:
            TOKENS.remove(received_data['token'])
            del POSITIONS[received_data['token']]
            print(TOKENS)
            print(POSITIONS)

            return jsonify({}), 200
    else:
        return jsonify({}), 500


@app.route('/m/position', methods=['POST'])
def submit_position():
    data = {}
    received_data = request.get_json()
    
    if received_data['token'] not in TOKENS:
    
        return jsonify({}), 500

    else:

        print("{} position received {}".format(received_data['token'], received_data))
        data['token'] = received_data['token']
        data['lat'] = received_data['lat']
        data['lng'] = received_data['lng']
        data['accuracy'] = received_data['accuracy']
        data['bearing'] = received_data['bearing']
        data['time'] = received_data['time']
        data['speed'] = received_data['speed']
        data['provider'] = received_data['provider']

        POSITIONS[received_data['token']].append(data)

        return jsonify({}), 200
