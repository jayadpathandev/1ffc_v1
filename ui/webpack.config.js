const fs           = require('fs');
const path         = require('path');
const webpack      = require('webpack');
const TerserPlugin = require("terser-webpack-plugin");

module.exports = function(env, argv) {
    if (env.sourceName === undefined) {
        throw "Please define the [sourceName] to process/generate.";
    }
    if (env.targetDir === undefined) {
        throw "Please define the [targetDir] to write to.";
    }
    const input  = './src/' + env.sourceName + '.ts';
    const output = env.sourceName + '.js'
    const prod   = !!env.production;

    if (fs.existsSync(input) == false) {
        throw "Input file [" + input + "] does not exist.";
    }
    if (fs.existsSync(env.targetDir) == false) {
        throw "Output dir [" + env.targetDir + "] does not exist.";
    }

    var retval = {
        entry:   input,
        mode:    prod ? 'production' : 'development',
        devtool: 'source-map',
        module: {
            rules: [ {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            } ]
        },
        resolve: {
            extensions: ['.tsx', '.ts', '.js'],
            alias: {
                'jquery-ui': 'jquery-ui/dist/jquery-ui.min.js',
                modules: path.join(__dirname, "node_modules"),
            }
        },
        output: {
            filename: output,
            path: path.resolve(__dirname, env.targetDir),
        },
        plugins: [
            new webpack.ProvidePlugin({
                "$":"jquery",
                "jQuery":"jquery",
                "window.jQuery":"jquery"
            })
        ],
        externals: {
            bootstrap: 'bootstrap'
        },
        performance: {
            hints: false
        }
    };

    if (prod) {
        retval.optimization = {
            minimize: true,
            minimizer: [
                new TerserPlugin({
                    terserOptions: {
                        format: { comments: false },
                    },
                    extractComments: false,
                }),
                new TerserPlugin({
                    minify: TerserPlugin.uglifyJsMinify,
                    terserOptions: {},
                  }),
            ]
        }
    }

    return retval;
};