module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    openui5_preload: {
      component: {
        options: {
          resources: {
            cwd: '',
            prefix: '',
            src: [
              'WebContent/**/*.js',
              'WebContent/**/*.fragment.html',
              'WebContent/**/*.fragment.json',
              'WebContent/**/*.fragment.xml',
              'WebContent/**/*.view.html',
              'WebContent/**/*.view.json',
              'WebContent/**/*.view.xml',
              'WebContent/**/*.properties'
            ]
          },
          dest: '',
          compress: false
        },
        components: true
      }
    }
  });

  grunt.loadNpmTasks('grunt-openui5');

}
