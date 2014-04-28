# trims the resulting wav file to talk start and end
#
module Audio
  module Strategy
    class Trim < Base

      def input
        "#{name}.wav"
      end

      def backup
        "#{name}-untrimmed.wav"
      end

      def run
        FileUtils.mv(input, backup, verbose: true)
        trim
        input
      end

      def trim_cmd
        return "sox -V1 #{backup} #{input} trim #{start} =#{stop}" if start > 0

        "cp #{backup} #{input}" # no trim required
      end

      def start
        opts[:talk_start] - file_start
      end
      
      def stop
        opts[:talk_stop] - file_start
      end
      
      def outputs
        [ input, backup ]
      end

    end
  end
end
