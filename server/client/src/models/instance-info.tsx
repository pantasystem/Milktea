import { z } from "zod";

const InstanceInfoSchema = z.object({
  host: z.string(),
  name: z.string().nullable(),
  description: z.string().nullable(),
  clientMaxBodyByteSize: z.number().nullable(),
  iconUrl: z.string().nullable(),
  themeColor: z.string().nullable(),
});

type InstanceInfo = z.infer<typeof InstanceInfoSchema>;

export default InstanceInfo;
export {InstanceInfoSchema};
