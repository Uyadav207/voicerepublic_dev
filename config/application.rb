require File.expand_path('../boot', __FILE__)

require 'rails/all'

# Require the gems listed in Gemfile, including any gems
# you've limited to :test, :development, or :production.
Bundler.require(:default, Rails.env)

module Kluuu2
  class Application < Rails::Application
    # Settings in config/environments/* take precedence over those specified here.
    # Application configuration should go into files in config/initializers
    # -- all .rb files in that directory are automatically loaded.

    config.i18n.enforce_available_locales = true

    # Set Time.zone default to the specified zone and make Active
    # Record auto-convert to this zone.
    # Run "rake -D time" for a list of tasks for finding time zone
    # names. Default is UTC.
    config.time_zone = 'Berlin'

    # The default locale is :en and all translations from
    # config/locales/*.rb,yml are auto loaded.
    # config.i18n.load_path += Dir[Rails.root.join('my', 'locales', '*.{rb,yml}').to_s]
    config.i18n.default_locale = :de

    # configure some test-suite thingies
    config.generators do |g|
      g.test_framework( :rspec,
                        :fixtures => true,
                        :view_specs => false,
                        :helper_specs => false,
                        :routing_specs => false,
                        :controller_specs => true,
                        :request_specs => true )
      g.fixture_replacement :factory_girl, :dir => "spec/factories"
    end

    # attribute_protected/attr_accessible lock down
    config.active_record.whitelist_attributes = true

    # still needed with rails4 ?
    config.assets.paths << "#{Rails.root}/app/assets/fonts"

    # TODO use assets the right way and we won't need these hacks, ask phil
    config.assets.precompile += %w( *.js )
    config.assets.precompile += %w(
      landing_page.css
      venues.css
      users.css
      dashboard.css
    )
  end
end

